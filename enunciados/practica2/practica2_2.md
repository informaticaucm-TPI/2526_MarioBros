# Índice

- [Práctica 2 - Parte II: Mario Extended](#práctica-2---parte-ii-mario-extended)
- [AddObjectCommand y factoría de objetos](#AddObjectCommand-y-factoría-de-objetos)
  - [Formato de los objetos del juego](#formato-de-objetos-del-juego)
  - [Factoría de objetos](#factory)
  - [Comando `AddObjectCommand`](#comando-addobjectcommand)
	- [Nuevo nivel para crear escenarios](#draw-map)
- [Nuevos objetos: Box y Mushroom](#box-mushroom)
  - [Mushroom](#mushroom)
  - [Box](#box)
- [Pruebas](#pruebas)
- [Entrega](#entrega)
  
<!-- TOC end -->
<!-- TOC --><a name="práctica-2-parte-ii-mario-extended"></a>
# Práctica 2 - Parte II: Mario Extended

**Entrega: Semana del 17 de noviembre**

**Objetivos:** Herencia, polimorfismo, clases abstractas e interfaces.

**Preguntas Frecuentes**: Como es habitual que tengáis dudas (es normal) las iremos recopilando en este [documento de preguntas frecuentes](../faq.md). Para saber los últimos cambios que se han introducido [puedes consultar la historia del documento](https://github.com/informaticaucm-TPI/2425-Lemmings/commits/main/enunciados/faq.md).

En esta práctica vamos a extender el código con nuevas funcionalidades. El principal objetivo de esta práctica será añadir nuevos objetos y nuevos comandos al juego. 

Antes de comenzar, tened en cuenta la **advertencia**:

> La falta de encapsulación, el uso de métodos que devuelvan listas, y el uso de `instanceof` o `getClass()` (fuera de metodos ``equals(Object other)``) tiene como consecuencia un **suspenso directo** en la práctica. Es incluso peor implementar un `instanceof` casero, por ejemplo así: cada subclase de la clase `GameObject` contiene un conjunto de métodos `esX`, uno por cada subclase X de `GameObject`; el método `esX` de la clase X devuelve `true` y los demás métodos `esX` de la clase X devuelven `false`.


<!-- TOC --><a name="AddObjectCommand-y-factoría-de-objetos"></a>

# AddObjectCommand y factoría de objetos

Nos planteamos realizar una extensión inédita en Mario, inspirada en juegos como Minecraft.
La extensión consiste en permitir que los usuarios creativos puedan crear sus propios mapas o modificar los ya existentes, y jugar en ellos. Es decir, añadiremos el *modo creativo* al juego de Mario.

Para ello será necesario crear una factoría de objetos, replicando la técnica de la factoría de comandos, un nuevo comando ``AddObjectCommand``, y además de un mapa, para poder generar cualquier escenario.

<!-- TOC --><a name="formato-de-objetos-del-juego"></a>
## Formato de los objetos del juego

Antes de hablar sobre el comando ``AddObjectCommand``, hemos de saber que para poder añadir un objeto al ``Game`` desde un ``String`` es necesario establecer un formato sobre los datos contenidos en  dicho ``String``. A continuación vamos a explicar el formato mediante unos ejemplos sencillos. 

El siguiente ejemplo representa un array de Strings, donde cada ``String`` representa a un objeto:

````
String[] a = {
    "(1,2) MARIO RIGHT BIG",
    "(0,1) LAND",
    "(3,2) GOOMBA RIGHT",
    "(2,7) EXITDOOR"
};
````

En primer lugar tenemos:
````
"(1,2) MARIO RIGHT BIG"
```` 
que se corresponde con un Mario situado en la fila 1, columna 2. Con dirección paso derecha; ``RIGHT``(la dirección, en este caso, solo podría ser o ``LEFT`` o ``RIGHT`` o ``STOP``, y aunque esté en el aire, será importante de cara a cómo se dibuja el Mario y a qué dirección va a utilizar una vez deje de estar en el aire). Por último aparece el valor ``BIG``  que indica que es grande (``isBig = true``), para indicar que no es grande, en lugar de un ``BIG`` tendríamos un ``SMALL``. También se debería poder crear un Mario con ``(1,2) MARIO`` que lo crearía en sus valores por defecto que son ``RIGHT`` y ``BIG``, o ``(1,2) MARIO LEFT`` que crearía un Mario con dirección de paso hacia la izquierda y grande.

En la siguiente posición tenemos 

````
   "(0,1) LAND",
````
esto nos indica que en la fila 0 columna 1, hay un Land. 

Seguidamente, tenemos que 
````
"(3,2) GOOMBA RIGHT",
````
indica que hay un Goomba en la fila 3 columna 2 cuya dirección de paso es derecha. También se debe permitir una línea del tipo ``(3,2) GOOMBA`` que añadiría un Goomba en su dirección por defecto ``LEFT``

Y finalmente tenemos:
````
 "(2,7) EXITDOOR"
````
que representa una puerta de salida en la fila 2 columna 2. 

Como podéis observar, en todos los casos el patrón es muy simple:
````
    posicionDelObjeto tipoDeObjeto atributosDelObjeto
````

Además, se ha de tener en cuenta que para los objetos se podrán usar sus variantes cortas (``M``, ``G``, ``L`` y ``ED`` para ``Mario``, ``Goomba``, ``Land``  y ``ExitDoor`` respectivamente. Pudiendo también utilizar las abreviaturas ``L`` y ``R``para las acciones ``LEFT`` y ``RIGHT`` respectivamente, y ``B`` y ``S``para los tamaños de Mario ``BIG`` y ``SMALL`` respectivamente.

<!-- TOC --><a name="factory"></a>
## Factoría de objetos

A continuación, tendremos que añadir una **factoría** de objetos a nuestra práctica. Como ya hemos visto para los comandos, la factoría se encarga de separar la lógica de la creación de un objeto del lugar en el que se crea.

Esta factoría estará implementada mediante una clase ``GameObjectFactory`` en el paquete ``gameobjects`` con un método 

```java
	public static GameObject parse (String objWords[], GameWorld game);
```
Usando esta factoría, para crear un objeto cuyas características vengan dadas por un array de Strings ``objWords``, bastaría hacer:

```java
GameObject gameobject = GameObjectFactory.parse(String objWords[], GameWorld game);
```

Fijaos que al crear el objeto no conocemos cuál es su tipo. La implementación de este método seguirá una estructura similar a la del método ``parse`` de ``CommandGenerator``. ``GameObjectFactory`` mantendrá una lista de objetos disponibles, al igual que hacía ``CommandGenerator`` con los comandos:

```java
private static final List<GameObject> availableObjects = Arrays.asList(
	new Land(),
	new ExitDoor(),
	new Goomba(),
	new Mario(),
	......
);
```
El método ``parse()`` deberá, a su vez, llamar a otro método ``parse`` desde cada objeto de la lista. Si la descripción dada por ``objWords`` se corresponde con algún objeto, deberá devolver una instancia de dicho objeto con las características indicadas. Si no se corresponde con ninguno, deberá devolver ``null``.  Podrá existir un método ``parse`` en la clase ``GameObject`` que será sobreescrito en las clases de aquellos objetos menos sencillos. 

Observa que para crear los objetos en la lista `availableObjects` necesitamos que estos tengan un constructor sin argumentos. ¿Qué modificador de acceso necesita dicho constructor?


<!-- TOC --><a name="comando-addobjectcommand"></a>
## AddObjectCommand
A continuación, vamos a definir un comando, ``AddObjectCommand``, el cual va a posibilitar que el usuario pueda añadir un objeto/personaje de cualquier tipo en cualquier posición del tablero. Las principales características del mismo son las siguientes:


````
Nombre: addObject
Abreviatura: aO
Detalles: [a]dd[O]bject <object_description>
Ayuda: adds to the board the object given by object_description
````

- El argumento `<object_description>` del comando es obligatorio y se corresponde con una línea de objeto que deberá seguir el formato indicado al comienzo de esta sección.
- Por ejemplo, al ejecutar `ao (14,3) Land` se añadirá un objeto `Land` en `(14,3)`. Y al ejecutar ``aO (13,3) Goomba RIGHT`` se añadirá en la posición ``(13,3)`` un objeto ``Goomba`` cuya dirección de paso es ``RIGHT``.  
- Si la descripción del objeto no se puede parsear o la posición dada está fuera del tablero, el comando mostrará el siguiente mensaje de error: ``Invalid game object: <stringObjeto>``.
- Para parsear el comando o lanzar el mensaje de error te resultarán de utilidad los siguientes métodos:
	- `Arrays.copyOfRange(words, from, to)`: devuelve una copia del array `words` desde el índice `from` (incluido) a `to` (excluido).
	- `String.join(" ", words)`: devuelve un `String` construido juntando los strings del array `words`, separándolos mediante espacios.

<!-- TOC --><a name="draw-map"></a>
### Nuevo nivel para crear escenarios

Para poder crear escenarios desde cero usando este comando, habrá que crear un nuevo nivel ``-1`` en el juego. Este nivel será un mapa vacío con 3 vidas, 100 unidades de tiempo y 0 puntos. Este mapa, a diferencia de los anteriormente vistos, incluye las vidas. Por tanto, cuando se resetee este mapa, también se reseteará el número de vidas a 3. 
![mapa-1](imgs/mapa-1.png)

<!-- TOC --><a name="#box-mushroom"></a>
# Nuevos objetos: Box y Mushroom

Ahora que ya tenemos nuestra factoría de objetos, además de la clase ``GameObject`` que nos permite utilizar el polimorfismo, vamos a añadir al juego un par de nuevos objetos. Estos serán la típica caja del juego de Mario ``Box`` y la seta que convierte a Mario de pequeño a grande ``MushRoom``.

<!-- TOC --><a name="mushroom"></a>
## Mushroom
Las características de este objeto serán  las siguientes:
````
Nombre: Mushroom
Abreviatura: MU
Icono: 🍄
````

Este objeto representará a la clásica seta del juego de Mario. Será representado por el icono **🍄**. Se considera un objeto no sólido. Este objeto interactuará con ``Mario``, si se encuentra en la misma posición que este, de la siguiente forma: si ``Mario`` es grande, el ``Mushroom`` desaparece y si ``Mario`` es pequeño se hace grande y el ``Mushroom`` desaparece. Con el resto de objetos no mantiene ninguna interacción. A diferencia de los `Goombas` la seta se dirige inicalmente a la derecha para que a `Mario` le resulte más difícil cogerla.

<!-- TOC --><a name="box"></a>
## Box
Finalmente, añadiremos un objeto que representará a la clásica caja de Mario. Las características principales del objeto son las siguientes:
````
Nombre: Box
Abreviatura: B
Icono: ? (si aún no ha sido abierto) y 0 (si ya se ha abierto)
Puntos: 50
````
Este objeto se considera un objeto sólido y no movible. En la primera colisión **desde abajo** entre Mario y la caja, aparecerá un ``MushRoom``, la caja se vaciará y dará a Mario 50 puntos. Para las demás interacciones con cualquier objeto esta actuará como un ``Land`` y, por tanto, no se llevará a cabo ninguna acción.

En la vista con colores está ya implementado de la siguiente forma: Si la caja no está vacía se representará con el icono **?** sobre un fondo gris, mientras que si la caja sí está vacía, se representará únicamente con un fondo gris. 

Para probar estas estensiones crear un nuevo nivel ``2`` que sea exactamente igual el nivel ``1`` pero con una caja en la posición `(9,4)` y dos setas en las posiciones `(12,8)` y `(2,20)`. Las posiciones indicadas siguen el formato: ``(fila,columna)``.

Ten en cuenta que también puedes probar dichos objetos a través del comando definido antes ``AddObjectCommand``. Para ello se podrá utilizar los siguientes formtatos de las cajas ``"(3,6) Box"``, ``"(3,5) Box Full"``, ``"(3,4) Box Empty"`` que reprentan una caja llena para las dos primeras y vacía para la última. También podrán utilizarse las abreviaturas ``F``y ``E`` para ``Full`` y ``Empty`` respectivamente.

<!-- TOC --><a name="pruebas"></a>
## Pruebas

Junto con este enunciado se acaban de añadir al GitHub las pruebas de estas extensiones (clase `tp1.Tests_V2_2`). Recuerda que tú código debe pasarlas. Para realizar estas pruebas, incluye a tu proyecto dicho fichero y la carpeta `tests/pr2_2`


<!-- TOC --><a name="entrega"></a>
## Entrega
La práctica debe entregarse utilizando el mecanismo de entregas del campus virtual, no más tarde de la **fecha y hora indicada en la tarea del campus virtual**.

El fichero debe tener, al menos, el siguiente contenido [^1]:

- Directorio `tp1` con el código de todas las clases de la práctica.
- Fichero `alumnos.txt` donde se indicará el nombre de los componentes del grupo.

Recuerda que no se deben incluir los `.class`.

> **Nota**: Recuerda que puedes utilizar la opción `File > Export` para ayudarte a generar el .zip.

[^1]: Puedes incluir también opcionalmente los ficheros de información del proyecto de Eclipse

