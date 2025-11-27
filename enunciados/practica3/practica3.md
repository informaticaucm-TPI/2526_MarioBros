# Índice
<!-- TOC start -->
- [Práctica 3: Excepciones y ficheros](#practica-3-excepcionesYficheros)
- [Introducción](#introduccion)
- [Manejo de excepciones](#exceptions)
	- [Excepciones en los comandos y controlador](#command-exceptions)
	- [Excepciones en `GameModel`](#gamemodel-exceptions)
- [Manejo de ficheros](#files)
	- [Serialización / Deserialización](#serialization)
		- [Formato de la Serialización / Deserialización](#serialization-format)
	- [Guardando la configuración del juego en un fichero: comando `SaveCommand`](#save-command)
	- [Cargando el juego desde fichero: comando ``LoadCommand``](#load-from-file)
		- [Excepciones en la factoría de objetos del juego](#object-factoy-exceptions)
		- [*Configuración del juego*: GameConfiguration](#game-configuration)
		- [Clase encargada de leer la configuración del fichero: FileGameConfiguration](#fileGameConfiguration)
		- [Modificación de la clase Game](#game-load)
		- [Comando de carga: LoadCommand](#load-command)
		- [Errores durante la carga del fichero](#file-exceptions)
	    - [Ajustando el método `reset` de Game](#reset-load-game)
	- [Sacando las configuraciones iniciales de la clase Game (opcional)](#level-conf)
- [Pruebas](#testing)
- [Entrega](#submission)
<!-- TOC end -->
<!-- TOC --><a name="practica-3-excepcionesYficheros"></a>
# Práctica 3: Excepciones y ficheros

**Entrega: semana del 1 de diciembre**

**Objetivos:** Manejo de excepciones y tratamiento de ficheros

**Preguntas Frecuentes**: Como es habitual que tengáis dudas (es normal) las iremos recopilando en este [documento de preguntas frecuentes](../faq.md). Para saber los últimos cambios que se han introducido [puedes consultar la historia del documento](https://github.com/informaticaucm-TPI/2425-Lemmings/commits/main/enunciados/faq.md).

<!-- TOC --><a name="introduccion"></a>
# Introducción

En esta práctica se ampliará la funcionalidad del juego en dos aspectos:

- Incluiremos la definición y el tratamiento de excepciones. Durante la ejecución del juego pueden presentarse estados excepcionales que deben ser tratados de forma particular. Por ahora, muchos de estos estados excepcionales los hemos tratados con la devolución del valor `null` pero seguro que ya has sufrido muchos errores del tipo `NullPointerException` y entiendes el motivo por el que no es adecuado el manejo de objetos nulos en Java. Además, al lanzar las excepciones allí donde se producen, los mensajes de error pueden ser más descriptivos y proporcionar al usuario información relevante de por qué se ha llegado a ellos (por ejemplo, errores producidos al procesar un determinado comando). El objetivo último es dotar al programa de mayor robustez, así como mejorar la interoperabilidad con el usuario.

- Cargaremos de ficheros las configuraciones iniciales del tablero. De esta forma las configuraciones iniciales no serán parte del código del programa ni estarán limitadas a unas pocas configuraciones fijas.
  
<!-- TOC --><a name="exceptions"></a>
# Manejo de excepciones

El tratamiento de excepciones en un lenguaje como Java resulta muy útil para controlar determinadas situaciones que se producen durante la ejecución del juego.

En esta sección se enumerarán las excepciones que deben tratarse durante el juego, se explicará la forma de implementarlas y se mostrarán ejemplos de ejecución.
Hemos dividido la sección en dos grandes bloques, uno para las excepciones que se producen en los comandos y controlador, y otro para las excepciones que se producen en el modelo.
Las excepciones relativas a los ficheros serán explicadas en la sección siguiente. 

Para simplificar se recomienda incluir todas las excepciones en el paquete `tp1.exceptions`.

<!-- TOC --><a name="command-exceptions"></a>
## Excepciones en los comandos y controlador

Una de las principales modificaciones que realizaremos a la hora de incluir el manejo de excepciones en el juego consistirá en ampliar la comunicación entre los comandos y el controlador. Para ello cambiaremos algunos métodos para que en una situación de error, en vez de devolver `null`, lancen una excepción.

Básicamente, los cambios que se deben realizar son los siguientes:

- Se debe definir una excepción general `CommandException` y como subclases dos excepciones concretas:

	- `CommandParseException`: excepción para errores que tienen lugar al "parsear" un comando, es decir, aquellos producidos durante la ejecución del método `parse()`, tales como comando desconocido, número de parámetros incorrecto o tipo de parámetros no válido.
	
	- `CommandExecuteException`: excepción para representar situaciones de error que se pueden dar al ejecutar el método `execute()` de un comando; por ejemplo, solicitar al juego que añada un objeto que no existe.

- La cabecera del método `parse(String[] commandWords)` del interfaz `Command` pasa a poder lanzar excepciones de tipo `CommandParseException`:

	```java
    public Command parse(String[] commandWords) throws CommandParseException;
	```

	Por ejemplo, el método `parse()` de la clase `NoParamsCommand` que se proporcionó en la práctica anterior pasa a ser de la siguiente forma:

	```java
	public Command parse(String[] commandWords) throws CommandParseException {
		if (commandWords.length > 1 && matchCommandName(commandWords[0]))
			throw new CommandParseException(Messages.COMMAND_INCORRECT_PARAMETER_NUMBER);
		
		Command cmd = null;
		if (commandWords.length == 1 && matchCommandName(commandWords[0]))
			cmd = this;
		
		return cmd;
	}
	```

   Fíjate en que los comandos lanzan excepciones solo cuando la entrada concuerda con el nombre (y por lo tanto deberían ser capaces de parsear) pero existe algún fallo en los argumentos pasados a dicho comando (y se da un error).  Es decir, la situación en la que el comando simplemente no puede parsear la entrada porque no concuerda con el nombre no se trata de un error (en cuyo caso no se lanza la excepción sino que se devuelve `null`).

- La cabecera del método estático `parse(String[] commandWords)` de `CommandGenerator` pasa a poder lanzar excepciones de tipo `CommandParseException`:

	```java
	public static Command parse(String[] commandWords) throws CommandParseException
	```

	de forma que el método lanza una excepción del tipo 

	```java
	throw new CommandParseException(Messages.UNKNOWN_COMMAND.formatted(commandWords[0]));
	```

	si se encuentra con un comando desconocido, en lugar de devolver `null` y esperar a que `Controller` trate el caso mediante un simple *if-then-else*.


- La excepcion `NumberFormatException` (que se lanza cuando se produce un error al intentar convertir un `String` a un valor numérico `int`, `long`, `float`, etc., pues el `String` no contiene una representación válida de dicho valor numérico) se debe detectar en el método `parse` del comando donde se produce y envolverla en una excepción del tipo `CommandParseException`. Ej. reset.

  Por ejemplo, en el método `parse` del comando `reset` podemos hacer lo siguiente:

	```java
	} catch (NumberFormatException nfe) {
		throw new CommandParseException(Messages.LEVEL_NOT_A_NUMBER_ERROR.formatted(commandWords[1]), nfe);
	}
	```
  
   El término «envolver» se refiere a la **buena práctica** de capturar una excepción de nivel inferior y lanzar una excepción de nivel superior que la incluya (lo que se consigue pasando la excepción de nivel inferior como argumento al constructor de la excepción de nivel superior) y *que contenga un mensaje menos específico que el de nivel inferior*[^1].


[^1]: Probablemente hayas visto ejemplos de malas prácticas en este sentido, por ejemplo, aplicaciones web de comercio electrónico que generan mensajes para el usuario del tipo «SQLException...». A un usuario normal no le interesan los detalles de implementación de este nivel y no debería recibir este tipo de mensajes; en su lugar, debería recibir un mensaje de alto nivel del tipo: «No podemos atender su solicitud en este momento, inténtelo más tarde». Sin embargo, la información de bajo nivel debería seguir estando disponible, por si fuera necesario.



- La cabecera del método `execute()` también se modifica indicando que puede lanzar excepciones de tipo `CommandExecuteException`:

	```java
	public void execute(GameModel game, GameView view) throws CommandExecuteException;
	```

- Como ya se mencionó, ambos tipos de excepciones, ``CommandParseException`` y ``CommandExecuteException``, llegan al controlador, que se encarga de capturarlas y enviar los mensajes que contienen a la vista para su visualización. En el caso de que una excepción encapsule una excepción de nivel inferior (que a su vez puede encapsular una excepción de nivel aún más bajo, etc.), en nuestra aplicación, el controlador envía todos los niveles de mensajes de error a la vista para su visualización.

  Por lo que, el método `run` del controlador ahora contendrá el siguiente código: 

	```java
	public void run() {
		...
		while (!game.isFinished()) {
			...
			try { ... }
			catch (CommandException e) {
				view.showError(e.getMessage());
				Throwable cause = e.getCause();
				while (cause != null) {
					view.showError(cause.getMessage());
					cause = cause.getCause();
				}
			}
		}
		...
	}
	```
  En una aplicación real, un usuario común probablemente no estaría interesado en ver todos los niveles de mensajes de error. En su lugar, bastaría con un mensaje sencillo y comprensible que indique el motivo por el cual no se puede ejecutar el comando. Sin embargo, un administrador o desarrollador podría necesitar información más detallada, por lo que sería necesario contar con los detalles completos del error para registrarlos adecuadamente en los logs.

<!-- TOC --><a name="gamemodel-exceptions"></a>
## Excepciones en `GameModel`

Como hemos comentado anteriormente, los errores al ejecutar los comandos surgen de la lógica del juego. En la práctica anterior los métodos invocados por los comandos susceptibles de generar errores devolvían un `boolean` para indicar si su invocación había tenido éxito. Por ejemplo, el método `addObject(String[] words)` de `GameModel` devolvía `false` cuando la posición se encontraba fuera del tablero o cuando se intentaba crear un objeto que no existe. 
Sin embargo, al agrupar ámbos comporatamientos el mensaje de error que podíamos generar se limitaba a informar de que el objeto no había podido añadirse en esa posición, sin concretarse si la posición se encontraba fuera del tablero o era un objeto no valido.

En general, los mensajes de error son más descriptivos si se crean allá donde se ha producido el error. Por ello, vamos a considerar que hay un error en cualquiera de las situaciones anteriores. Se podrían controlar más situaciones de errores, pero con estas vale para ilustrar el concepto.
Esto nos permitirá mostrar los siguientes mensajes de error:

- Mensaje al intentar añadir un objeto inexistente:

	```
	[DEBUG] Executing: addObject (3,2) poTaTo

 	[ERROR] Error: Command execute problem
 	[ERROR] Error: Unknown game object: "(3,2) poTaTo"

	```

- Mensaje al intentar establecer un objeto en una posición fuera del tablero:

	```
	[DEBUG] Executing: addObject (-4,24) Land

	[ERROR] Error: Command execute problem
	[ERROR] Error: Object position is off board: "(-4,24) Land"
	```
<!--- PARSER errors

- Mensaje al intentar establecer un objeto en una posición que ya contiene un objeto sólido:

	```
	[DEBUG] Executing: addObject (2,2) Land

	[ERROR] Error: Command execute problem
	[ERROR] Error: A solid object is in Position (2,2)
	```
---> 


Por lo tanto, la cabecera del método `addObject` de `GameModel` quedará así:

```java
public void addObject(String[] objWords) throws OffBoardException, ObjectParseException;
```

Además, consideraremos las siguientes excepciones lanzadas por los métodos de `GameModel`. Todas ellas heredarán de una clase `GameModelException`.

- `OffBoardException`: excepción que se produce cuando se intenta acceder de manera indebida a una posición fuera del tablero (por ejemplo, al tratar de colocar un objeto fuera del tablero).

- `GameParseException`: excepción que se produce al parsear un objeto del juego. Como subclases de esta clase se considerarán las siguientes excepciones:

	- `ActionParseException`: excepción lanzada por la clase `Action` al tratar de generar una acción con un string que no corresponda a ninguna de las que existen.

	- `PositionParseException`: excepción lanzada por la clase `Position` al tratar de generar una posición con un string que no sigue el formato adecuado.
 
	- `ObjectParseException`: excepción que se produce al tratar de parsear un objeto en formato texto y no poder convertirlo al objeto correspondiente, pues no sigue el formato establecido. La veremos en la sección de los ficheros.

Volviendo a los errores de ejecución de los comandos, el método `execute` de cada comando  invocará a un método de `GameModel` susceptible de lanzar las excepciones listadas más arriba. En cada caso deberemos construir la nueva excepción `CommandExecuteException` de manera que 
*recubra* o *decore* a la excepción capturada. Por ejemplo:

```java
} catch (OffBoardException obe) {
	throw new CommandExecuteException(Messages.ERROR_COMMAND_EXECUTE, obe);
}
```

De esta forma la causa última del error (la excepción `obe`) no se pierde y, en particular, se puede recuperar el mensaje de la excepción recubierta en el controlador (fíjate en el `cause.getMessage()` que aparece en el método `run` del controlador).

<!-- TOC --><a name="files"></a>
# Manejo de ficheros

<!-- TOC --><a name="serialization"></a>
## Serialización / Deserialización

En informática, el término **serialización** se refiere a convertir estructuras de datos en una secuencia de bytes, generalmente con el objetivo de guardar esta secuencia en un archivo o transmitirla a través de una red. Comúnmente, la estructura que se serializa es el estado actual de un programa en ejecución, o de parte de un programa en ejecución. El término **deserialización** hace referencia al proceso inverso de reconstruir las estructuras de datos, comúnmente el estado de un programa en ejecución, o parte de un programa en ejecución, a partir de una secuencia de bytes.

La serialización/deserialización, en la que la secuencia generada es una secuencia de texto, a veces se conoce como **stringificación/destringificación**. Claramente, el formato utilizado para la serialización/stringificación debe diseñarse de manera que facilite la deserialización/destringificación.

Java incorpora un mecanismo de serialización genérico capaz de convertir cualquier objeto de Java, y por lo tanto cualquier programa en ejecución de Java o parte de cualquier programa en ejecución de Java, en una secuencia binaria (ver las clases ``ObjectInputStream`` y ``ObjectOutputStream``).

No requerimos un mecanismo de serialización/deserialización genérico como ese; nuestro interés se limita a serializar a, y deserializar desde, una secuencia de texto que representa el estado actual de nuestro juego, con el objetivo de guardar este estado en un archivo de texto y ser capaz de leerlo desde dicho archivo.

Es importante señalar que la representación textual actualmente producida por la vista no es un formato adecuado para la serialización, ya que la deserialización de este formato sería una tarea bastante complicada. Parece más razonable definir el formato de serialización en texto como una secuencia de elementos del juego. Es decir, podríamos reutilizar la representación de los objetos utilizada por el ``AddObjectCommand``, y solo sería necesario definir el formato general para el estado del ``Game``.


<!-- TOC --><a name="serialization-format"></a>
### Formato de la Serialización / Deserialización

Para poder cargar una configuración de un `game` desde un fichero de texto es necesario establecer un formato sobre los datos contenidos en el fichero. En este caso explicaremos el formato a través de un ejemplo simple. 

El siguiente ejemplo representa un fichero de `game` con dos tierras, una puerta de salida, una caja vacía y dos goombas:

```
100 0 5
(14,0) Land
(14,1) Land
(14,2) ExitDoor
(11,1) Box Empty
(13,0) Mario RIGHT Small
(13,1) Goomba RIGHT
(13,1) Goomba LEFT
```

En la primera línea `100 0 5` se encuentra el estado del game. Como se puede observar el estado está formado por 3 números enteros, por orden de aparición en el que se encuentran corresponden con el *tiempo restante* , los *puntos*  y  las *vidas*. Tras esta primera línea aparecen los objetos del juego siguendo el formato visto en la Práctica 2.


<!-- TOC --><a name="save-command"></a>
## Guardando la configuración del juego en un fichero: comando `SaveCommand`

Después de definir nuestro formato de serialización, el siguiente paso lógico es introducir un nuevo comando,
el comando `save`, que se utiliza para guardar el estado actual serializado del juego en un archivo.

Recuerda, que tal como indica la documentación de Java, el método `toString()` debería devolver la representación textual legible para humanos del estado del game, mientras que la serialización solo sería necesario que fuera legible por la máquina. En nuestro caso, como el formato que hemos elegido mantiene ámbas propiedades podríamos utilizar dicho método para realizar la *stringificación*. En cualquier otro caso sería necesario definir un nuevo método para para realizar dicha tarea,  que podría llamarse ``stringify``. Por lo tanto, si consigues que el método `toString()` lo devuelva en el formato visto, la tarea consiste únicamente en añadir un nuevo comando `SaveCommand` y un nuevo método en `Game`:

```java
	public void save(String fileName) throws GameModelException {...}
```

Este método solo arrojaría excepciones en caso de que se produjera un error en la escritura del fichero. Esto podría ocurrir, por ejemplo, en casos donde el fichero estuviera bloqueado por el sistema operativo.

El resultado final será el comando `save` del juego, cuya ayuda se mostrará de la siguiente forma:

```
Command > h
[DEBUG] Executing: h

Available commands:
   [s]ave <fileName>: save the actual configuration in text file <fileName>
   ...
```

<!-- TOC --><a name="load-from-file"></a>
## Cargando el juego desde fichero: comando ``LoadCommand``


En este apartado nuestro objetivo será conseguir *cargar una configuración del juego* contenida en un fichero de texto. Para realizar esta tarea utilizaremos varias técnicas de programación aprendidas anteriormente.

El resultado final será el comando `load` del juego.

Como ya hemos establecido el formato de la serialización y disponemos de una factoría de objetos que procesa dicha formato, la tarea no parece especialmente complicada. Aún así, dividiremos la tarea en las siguiente tareas más pequeñas:


- [añadiremos excepciones a la factoría de objetos del juego](#object-factoy-exceptions),

- [definiremos un interfaz `GameConfiguration`](#game-configuration) que será implementado por las clases que procesen o mantengan una configuración del juego,

- [crearemos la clase `FileGameConfiguration`](#configuraciones-iniciales), encargada de la lectura de la configuración del fichero de texto. Esta clase delegará parte de sus tareas en la factoría de objetos,

- [extenderemos los servicios del game´,](#game-load) añadiendo el nuevo servicio ``load`` que permita cargar dichas configuraciones y 

- [crearemos la clase `LoadCommand`](#load-command). 

Al finalizar estas modificaciones será necesario comprobar los mensajes de errores que se producirán en la carga del fichero y el comportamiento adecuado del comando `reset` que se verá afectado, pues corresponde con la carga de la última configuración del juego.

Se recomienda en una primera etapa realizar la carga del fichero considerando que el formato del fichero es correcto y posteriormente ajustar el código para que sea capaz de detectar errores en el fichero.


<!-- TOC --><a name="object-factoy-exceptions"></a>
### Excepciones en la factoría de objetos del juego

Al igual que hicimos con la factoría de comandos ahora te toca añadir excepciones a la factoría de objetos, para evitar que dicha factoría devuelva objetos `null`:

- `ObjectParserException`: excepción lanzada cuando no se puede analizar la línea porque su formato es incorrecto, por ejemplo por no tener todos los datos necesarios, por tener más datos de los necesarios, por tener un nombre de objeto desconocido, etc.

- `OffBoardException`: excepción lanzada cuando la posición se encuentra fuera del tablero.
	
Fíjate en que, al igual que ocurría con la clase `CommandGenerator`, la factoría de objetos **nunca** devuelve el valor `null`: o bien tiene éxito al crear el objeto o bien lanza una excepción[^2].


[^2]: Seguimos la misma técnica que el método [Integer.valueOf](https://docs.oracle.com/javase/8/docs/api/java/lang/Integer.html#valueOf-java.lang.String- "Integer.valueOf"). 


<!-- TOC --><a name="game-configuration"></a>
### *Configuración del juego*: GameConfiguration
Antes de empezar la tarea es conveniente que nos planteemos la siguiente cuestión:

```¿Qué consideramos que es una "configuración del juego"?```

 Una posible respuesta es que una configuración consiste en unos cuantos valores enteros indicando el tiempo restante, los puntos, las vidas y los objetos del juego. Dependiendo de si en tu implementación mantienes la referencia en `Game` a *mario** necesitaras distinguir a Mario del resto de objetos del juego. Por lo que, si mantienes esa referencia, podríamos considerar que una **configuración del juego** es un objeto que es capaz de suministrar esos valores:
 
 ```java
	// game status
	public int getRemainingTime();
	public int points();
	public int numLives();
	// game objects
	public Mario getMario();
	public List<GameObject> getNPCObjects();
 ```

 Si no mantienes en Game dicha referencia a *mario* no necesitarás el método ``getMario`` y sería conveniente que cambiaras el nombre del método ``getNPCObjects`` por ``getObjects``.
 
 Tu primera tarea consiste en crear dicho interfaz.

<!-- TOC --><a name="fileGameConfiguration"></a>
### Clase encargada de leer la configuración del fichero: FileGameConfiguration

Como principio general, la lectura de un archivo nunca debe
1. provocar el bloqueo del programa,
2. ni dejarlo en un estado incoherente

```¿Cómo lo conseguimos?```

1. Capturar todas las excepciones que podrían producirse al leer un archivo garantiza que el programa no se bloquee. Si la carga falla, el programa puede gestionar la excepción, informar al usuario y continuar exactamente como si no se hubiera intentado la carga. Con este fin, para asegurarse de que no se pase por alto ninguna excepción, después de capturar excepciones específicas, el código debe incluir una cláusula que capture una excepción general,```catch (Exception e)```, envolviéndola, al igual que las demás excepciones lanzadas durante la carga, en una GameLoadException, véase más abajo.
2. Cargar los datos del archivo en una clase de propósito especial que solo se utiliza (en nuestro caso, como el nuevo estado del juego) si y cuando los datos se han cargado completa y correctamente desde el archivo es una forma de garantizar que el programa no pueda quedar en un estado incoherente. En nuestro caso, esta clase de propósito especial es la clase ``FileGameConfiguration``, que implementa la interfaz ``GameConfiguration`` y se coloca en el paquete ``tp1.logic``.

 Para garantizar que un estado incoherente del juego nunca se pueda utilizar como estado del juego, se facilita la carga desde el archivo en el constructor de la clase ``FileGameConfiguration``. Así, si la comprobación de validez es exhaustiva, los objetos de esta clase que encapsulan un estado incoherente del juego nunca pueden existir.

El constructor de la clase tendrá dos parámetros: el nombre del fichero del cual realizar la carga y el `game`, para poder *enganchar* los objetos del fichero con el game en ejecución:

```java
public FileGameConfiguration(String fileName, GameWorld game) throws GameLoadException;
```

Como puede observarse en su cabecera puede lanzar una única excepción:
- `GameLoadException`: lanzada tanto en caso de que el fichero no exista `FileNotFoundException` (excepción estándar de Java), en el caso de que haya algún problema con la lectura o con el formato del fichero  `ObjectParserException` o en el caso de que alguna de las posiciones se encuentren fuera del tablero `OffBoardException` o cualquier otra excepción que pueda producirse durante la carga.

<!-- TOC --><a name="game-load"></a>
### Modificación de la clase Game

Añadiremos al modelo la opción de cargar configuraciones desde un fichero. Para ello sólo nos hará falta crear en `Game` un método público para poder cargar el juego a través del fichero y añadirlo en el interfaz adecuado:

```java
	public void load(String fileName) throws GameLoadException {...}
```

Este método simplemente crea un objeto `FileGameConfiguration`, con tipo `GameConfiguration`, y luego establece los atributos del juego con los valores devueltos por las llamadas a los métodos de la interfaz `GameConfiguration`. Como se puede observar, este método puede lanzar la misma excepción que el constructor de la clase `FileGameConfiguration` y además en los mismos casos.

<!-- TOC --><a name="load-command"></a>
### Comando de carga: LoadCommand
Ya estamos en situación de poder crear el comando nuevo de carga `LoadCommand`. Su tarea consistirá en cargar una configuración de game desde un fichero. Después de cargar el nuevo estado, siempre que éste haya tenido exito, el método `execute` de la clase `LoadCommand` debe mostrar el tablero.


La ayuda general tras la implementación de dicho comando será la siguiente:
```
Command > h
[DEBUG] Executing: h

Available commands:
   [l]oad <fileName>: load the game configuration from text file <fileName>
   [s]ave <fileName>: save the actual configuration in text file <fileName>
   [a]dd[O]bject <object_description>: adds to the board the object given by object_description.
      <object_description> = (col,row) objName [dir [BIG|SMALL]]. Ej. (12,3) Mario LEFT SMALL
   [a]ction [[R]IGHT | [L]EFT | [U]P | [D]OWN | [S]TOP]+: user performs actions
   [u]pdate | "": user does not perform any action
   [r]eset [numLevel]: reset the game to initial configuration if not numLevel else load the numLevel map
   [h]elp: print this help message
   [e]xit: exits the game

Command > 	
```

<!-- TOC --><a name="file-exceptions"></a>
### Errores durante la carga del fichero

Durante la carga de la configuración del juego del fichero se pueden producir muchas excepciones ya que es necesario comprobar que tanto el estado del juego almacenado como cada descripción de objeto (i.e., cada línea) es correcta. Es posible que estos errores los tengas que analizar en el método `parse` de `GameObject` o de alguna de sus subclases.

- Tiene que existir el fichero:

	```
	Command > load conf
	[DEBUG] Executing: load conf

	[ERROR] Error: Unable to load game configuration from file "conf"
	[ERROR] Error: File not found: "conf"
	[ERROR] Error: conf (El sistema no puede encontrar el archivo especificado)

	```

- La línea de estado del game mantiene el formato adecuado.

	```
	Command > load conf_2
	[DEBUG] Executing: load conf_2

	[ERROR] Error: Unable to load game configuration from file "conf_2"
	[ERROR] Error: Incorrect game status "2 5"
	```

- Los objetos que aparecen en el fichero son de tipo conocido:

	```
	Command > load conf_3
	[DEBUG] Executing: r conf_3

	[ERROR] Error: Unable to load game configuration from file "conf_3"
	[ERROR] Error: Invalid file "conf_3" configuration
	[ERROR] Error: Unknown game object: "(3,2) Potato RIGHT"
	```

- La posición de cada objeto dado está dentro del tablero:

	```
	Command > load conf_4
	[DEBUG] Executing: load conf_4

	[ERROR] Error: Unable to load game configuration from file "conf_4"
	[ERROR] Error: Invalid file "conf_4" configuration
	[ERROR] Error: Object position is off board: "(43,18) Goomba RIGHT"
	```


- Las direcciones de los objetos en movimiento son valores conocidos:

	```
	Command > load conf_5
	[DEBUG] Executing: r conf_5

	[ERROR] Error: Unable to load game configuration from file "conf_5"
	[ERROR] Error: Invalid file "conf_5" configuration
	[ERROR] Error: Unknown moving object direction: "(3,18) Goomba NORTH"
	[ERROR] Error: Unknown action: "NORTH"
    ```
 
- Las direcciones de los objetos en movimiento son sólo `RIGHT`, `LEFT` y `STOP`:

	```
	Command > load conf_6
	[DEBUG] Executing: r conf_6

	[ERROR] Error: Unable to load game configuration from file "conf_6"
	[ERROR] Error: Invalid file "conf_6" configuration
	[ERROR] Error: Invalid moving object direction: "(3,18) Goomba UP"
	```

- El tamaño de Mario es un valor válido:

	```
	Command > load conf_7
	[DEBUG] Executing: r conf_7

	[ERROR] Error: Unable to load game configuration from file "conf_7"
	[ERROR] Error: Invalid file "conf_7" configuration
	[ERROR] Error: Invalid Mario size: "(3,2) Mario RIGHT NORMAL"
	```

- Cada línea tiene el formato adecuado porque alguno de sus atributos no tiene el formato adecuado. Por ejemplo, los valores de las posiciones y la altura se deben poder convertir a números.

	```
	Command > load conf_8
	[DEBUG] Executing: load conf_8

	[ERROR] Error: Unable to load game configuration from file "conf_8"
	[ERROR] Error: Invalid file "conf_8" configuration
	[ERROR] Error: Invalid object position: "(a,3) Goomba RIGHT"
	[ERROR] Error: Invalid position: "(a,3)"
	[ERROR] Error: For input string: "a"
	```

Esas excepciones se capturarán directamente en el método `execute` de `LoadCommand`. Ten en cuenta que si la carga del fichero falla se gestiona esa excepción, informando al usuario del problema, y el juego debe poder continuar como si no se hubiese intentado la carga.

<!-- TOC --><a name="reset-load-game"></a>
## Ajustando el método `reset` de Game

Ahora que ya has completado la implementación, una de las cuestiones que te debes plantear es que el `reset` debe seguir funcionando correctamente. 
En particular, realizar un reset desde un juego cargado desde un fichero debería devolvernos a la configuración cargada desde este. Esto puede conseguirse haciendo que el método `load` de la clase `Game` almacene el objeto creado durante la carga, en un atributo de la clase `Game`:

```java
private GameConfiguration fileloader;
```

Por simplicidad, volveremos a emplear la técnica, aunque poco recomendable, de utilizar un valor ``null`` para indicar que debe realizarse el restablecimiento estándar. 
En caso contrario, se utilizará el último estado del juego cargado, almacenado en el objeto ``FileGameConfiguration``, ubicado en el atributo ``fileLoader`` del juego. 
Si realizas la tarea opcional de [**sacar las configuraciones del juego**](#level-conf) a otra clase este atributo nunca será nulo y, además, puedes hacer desaparecer el atributo ``nLevel`` del game.

Revisa que funciona correctamente dicho `reset`, pues es posible que sin darte cuenta tengas algún error de encapsulamiento en la clase `FileGameConfiguration` que descubras ahora.


<!-- TOC --><a name="level-conf"></a>
## Sacando las configuraciones iniciales de la clase Game (opcional)

Otra cuestión bastante simple consiste en sacar las configuraciones iniciales de la clase `Game` (las que se cargan usando `nLevel`), creando una clase que implemente `GameConfiguration` que se encargue de ellas `LevelGameConfiguration`. 
Esta clase sería similar a `FileGameConfiguration` y podría tener dos constructores: uno que reciba el *número del nivel*, que lanzaría una excepción en caso de que dicho nivel no sea válido, y otro sin el número de nivel, que cargaría el mapa `1`. De esta forma, reubicarías los métodos del ``initLevelXX`` a esta clase.

Si te animas y tienes tiempo es bastante sencilla su implementación y te permitirá tener un código más limpio y ordenado.

<!-- TOC --><a name="testing"></a>
## Pruebas

Junto con este enunciado se acaban de añadir al GitHub las pruebas de estas versión (clase `tp1.Tests_V3.java`) y se ha actualizado el fichero `tp1.Tests_Utils.java` para que indique más claramente un fallo debido a que una práctica finaliza antes de que la prueba completa termine.

En esta versión, además de los dos comandos nuevos ``load`` y ``save``, se ha añadido mucho detalle en el control de errores. Esto no debería cambiar el comoportamiento de la lógica del juego en caso de no se encontrarse con ningún error. Por ese motivo se ha decidio añadir todos los test de comportamiento de la *versión 2* que deberían pasarse de forma trivial.

Además, en esta entrega se ha incluido un **test opcional** que comprueba cuestiones del **comportamiento de Mario** que aún pareciendo naturales (como evitar que Mario "vuele", que atraviese objetos sólidos, etc.), no se habían añadido antes por no considerarse relavantes. Es importante destacar que este test **no es obligatorio** para la calificación, pero su superación asegura una implementación más robusta. No obstante, es imprescindible que tu código pase satisfactoriamente el resto de pruebas obligatorias del proyecto. Para realizar estas pruebas, incluye a tu proyecto el fichero dicho fichero `tp1.Tests_V3.java` y la carpeta `tests/pr3`.

<!-- TOC --><a name="submission"></a>
## Entrega
La práctica debe entregarse utilizando el mecanismo de entregas del campus virtual, no más tarde de la **fecha y hora indicada en la tarea del campus virtual**.

El fichero debe tener, al menos, el siguiente contenido [^1]:

- Directorio `tp1` con el código de todas las clases de la práctica.
- Fichero `alumnos.txt` donde se indicará el nombre de los componentes del grupo.

Recuerda que no se deben incluir los `.class`.

> **Nota**: Recuerda que puedes utilizar la opción `File > Export` para ayudarte a generar el .zip.

[^1]: Puedes incluir también opcionalmente los ficheros de información del proyecto de Eclipse

