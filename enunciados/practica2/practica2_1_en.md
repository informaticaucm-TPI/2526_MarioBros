<!-- TOC start -->
- [Práctica 2 - Parte I: Mario Refactored](#práctica-2---parte-i-mario-refactored)
	- [Introduction](#introducción)
	- [Refactoring the solution of the previous assignment](#refactorización-de-la-solución-de-la-práctica-anterior)
		- [The command inheritance hierarchy](#patrón-command)
		- [The game-object inheritance hierarchy](#herencia-y-polimorfismo)
		- [Simplifying the game-object container](#contenedor-de-objetos-del-juego)
		- [Generalizing the interactions between game objects](#generalizacion-interacciones)
		- [Interfaces implemented by the `Game` class](#interfaces-de-game)
	- [Testing](#pruebas)
<!-- TOC end -->
<!-- TOC --><a name="práctica-2-parte-i-lemmings-refactored"></a>
# Assignment 2 - Part I: Mario Refactored
 
**Objetivos:** Inheritance, polymorphism, abstract classes and interfaces.

<!--
**Preguntas frecuentes**: Como es habitual (y normal), que tengáis dudas, las iremos recopilando en este [documento de preguntas frecuentes](../faq.md). Para saber los últimos cambios que se han introducido [puedes consultar la historia del documento](https://github.com/informaticaucm-TPI/2324-SpaceInvaders-SOLUCION/commits/main/enunciados/faq.md).
-->

<!-- TOC --><a name="introducción"></a>
## Introduction

In this assignment, we apply the mechanisms that OOP offers to improve and extend
the code developed in the previous assignment as follows:

- In Part I of this assignment, we refactor[^1] the code of the previous assignment in the following
ways (involving two inheritance hierarchies):

  - Firstly, by removing some code from the controller `run` method and distributing
    its functionality among an inheritance hierarchy of classes, enabling the `Controller`
    class to be generic, i.e. it has no dependence on the exact commands used. This
    involves applying what is known as the *command pattern*.

  - Secondly, by defining an inheritance hierarchy of game objects, the leaves of which will be the classes that
    were used in the previous assignment, namely `Mario`, `Goomba`, `Ground` and `ExitDoor`,
    enabling the `Game` and `GameObjectContainer` classes to be generic, i.e. they have no
    dependence on the exact game objects used (**and they certainly do not use `isinstance`
    or `getClass`!**)
    The use of inheritance enables us to avoid having identical, or nearly identical, code
    in different classes. It also enables us to reorganise how we store the information
    about the state of the game, using a single data structure instead of multiple lists.
    
    To preserve the genericity of the `Game` and `GameObjectContainer` classes without
    introducing *ad-hoc* properties in the game objects we will introduce a generic
    mechanism for handling interactions between game objects using a technique called
    *double-dispatch*.
    
  - Thirdly, we add interfaces to be implemented by the `Game` class which provide three
    partial views of its public methods, thereby classifying its client classes into
    three groups (classes in the *View*, classes in the *Controller* and classes internal
    to the *Model*) and ensuring that each group can only invoke the subset of
    the `Game` class methods contained in its partial view.
    
  The aim of these changes is to produce code that is more extensible, adaptable, maintainable,
  robust, etc. without changing the functionality. Note that if the functionality has not changed, the
  refactored code should pass the same tests as the code submitted for Assignment 1.

- Once the code has been refactored, in part II of this assignment, we add new commands and new
  elements to the game, thereby demonstrating how the new structure facilitates these extensions.

In summary, the objective of the first part of Assignment 2 is to make the code more extensible,
adaptable, maintainable, robust, etc. without changing its functionality.

[^1]: Refactoring means changing the structure of code (to improve it, presumably) without changing its functionality.

<!-- TOC --><a name="refactorización-de-la-solución-de-la-práctica-anterior"></a>
## Refactoring the solution to the previous assignment

<!-- TOC --><a name="patrón-command"></a>
### The command inheritance hierarchy

In the application built in the previous assignment, the user could enter several different commands in order to update the game,
reset the game, ask for help, etc. The objective of the first part of the refactoring is to introduce
a structure to the part of the code that is concerned with processing the user commands which
will facilitate the addition of new commands, i.e. which will enable new commands to be added
with minimal modifications to the existing code. This structure is the well-known software
design pattern [^2] referred to as the *command pattern* [^3]. The general idea is to encapsulate each user
action in its own class where, in this case, the user actions are the commands.

The following classes are involved in our application of the *command pattern*:

-  `Command`: an interface that defines the public methods of any command, namely `execute`, `parse` and `helpText`.

- `AbstractCommmand`: an abstract class that encapsulates the functionality common to all the concrete
   commands. It has four attributes of type `String` that are initialised in its constructor -- `name`,
   `shortcut` (the abbreviated name), `details` (the initial part of its help message), `help`
   (the details of its help message) -- and *getter* methods for at least `name` and `shortcut` whose
   values are needed by the `parse` methods of the subclasses (instead of making these attributes
   `protected`).

- `NoParamsCommand`: an abstract class that inherits from `AbstractCommmand` and which is the superclass of all
   those concrete command classes that represent a command with no parameters, such as `HelpCommand`.
   The *raison d'être* of this class is that the parsing of all commands with no parameters only differs
   in the value of the `name` and `shortcut` attributes used to compare with the input text, so that the
   `parse` method of all the corresponding classes can be inherited from the `NoParamsCommand` class.
   Evidently, the same is not true of the `execute` method of the classes representing commands with no
   parameters, which will be different for every command, for which reason, the `NoParamsCommand` class
   must be abstract.

- Concrete command classes: `HelpCommand`, `ExitCommand`, etc., one such class for each program command.
  In the case of a command with parameters, the corresponding class has attributes to store the value
  of these parameters. Each concrete command class also contains the four attributes `name`, `shortcut`,
  `details` and `help` of the `AbstractCommand` class (but cannot access them directly since they are
  `private` to the `AbstractCommand` class). Though these attributes are assigned values in the
  constructor of the `AbstractCommand` class, the values used are specific to each concrete command class.
  This is achieved by passing these values up from the constructor of each concrete command class, in
  which they are defined as constants. For example, the code of the `HelpCommand` class has the following
  aspect:
    
  ```java
  public class HelpCommand extends NoParamsCommand {

	private static final String NAME = Messages.COMMAND_HELP_NAME;
	private static final String SHORTCUT = Messages.COMMAND_HELP_SHORTCUT;
	private static final String DETAILS = Messages.COMMAND_HELP_DETAILS;
	private static final String HELP = Messages.COMMAND_HELP_HELP;

	public HelpCommand(){
		super(NAME, SHORTCUT, DETAILS, HELP);
	}

    // Implementation of the execute method
  }
  ```
  
  Each concrete command class has (at least) the following four methods:

  - `protected boolean matchCommand(String)`: checks if the first word of the text introduced by the user
     via the keyboard (minimally processed) corresponds to the command name. It is called by the parse method.

  - `public Command parse(String[])`: checks if the text introduced by the user via the keyboard
     (minimally processed and placed in an array of strings) corresponds to a use of the command (which
     involves checking the number and syntactic validity of the parameter values [^4]):

     * If so, it returns an instance of the owning class, which then represents this use of the command.
       In the case of a command with no parameters, the `parse` method can simply return the value `this`.
       but in the case of a command with parameters, to avoid creating *fragile* code [^5], it must return
       a new instance of the command class, rather than changing the values of the attributes
       of the containing object and then returning `this`.

     * If not, it returns the value `null`.
  
  - `public void execute(Game, GameView)`: executes the functionality associated to the command by calling
    a method of the `Game` class, updating the view where necessary (in some commands, this method may also
    perform some other actions). In later versions of the program, command execution may fail, in which case
    updating the view will consist of printing an error message instead of printing the current state of the
    game.
    
  - `public String helpText()` which returns that part of the text concerning this command that is returned
    by the `help` command.

- `Controller`: the code of the controller class is now reduced to only a few lines since most of the
  functionality that it included in Assignment 1 is now delegated to the concrete command classes. Note
  that the *Controller* MVC component contains the `Controller` class and the classes of the `Command`
  class hierarchy [^6].

[^2]: You will study software design patterns in general, and this software
design pattern in particular, in the Software Engineering course.

[^3]: Strictly speaking, we use a slightly-modified version of the *command pattern*,
adapted to the needs of this assignment.

[^4]: **Design principles**: Syntactic validity of the arguments should be checked in the parsing of the *Controller*
MVC component whereas semantic validity of the arguments (e.g. a position is invalid due to being off the board)
should be checked in the *Model* MVC component. This is in accordance with the design principle known as
*Separation of Concerns*.

[^5]: **Design principles**: Fragile code is code which makes implicit assumptions about its
environment (where this refers to the rest of the program code together with the program environment)
which can lead to it working incorrectly if its environment changes and no longer fulfills these assumptions.
In the case in point, returning the value `this` is making the implicit assumption
that there will only ever be one instance of the corresponding command class in the program at the same time
(the one that is in the `AVAILABLE_COMMANDS` list). Command objects that represent commands without parameters
are *stateless* since, in the absence of attributes, an instance is simply a pointer to a set of methods;
for such objects the above-mentioned assumption is in no way restrictive. However, command objects that
represent commands with parameters are *stateful* and
though the above assumption holds for such commands in the current program, it may not hold for them in evolutions
of the program, e.g. a program where we store a stack of command objects in order to implement an *undo* command,
so why make this assumption, given that it is completely unnecessary to do so?

[^6]: **Design principles**: Already in Assignment 1, ensuring that the *Controller* MVC component did not
contain any logic was in accordance with the design principle known as *thin controllers, fat models*, a
property that we conserve in Assignment 2.
Ensuring that the central part of the *Controller* MVC component, i.e. the `Controller` class, is a high-level
class that does not depend on any concrete implementation is in accordance with the **D** of
the **SOLID** design principles (the ***D**ependency Inversion Principle*).

**Main loop of the program**. In the previous assignment, in order to know which command to execute,
the main loop of the program
in the `run` method of the controller contained a switch or if-else ladder with one option for each of
the commands. In the reduced version of the controller, the `run` method has the
following aspect (your code does not have be be exactly the same but should be similar):

```java
while (!game.isFinished()) {

    Command command = CommandGenerator.parse(view.prompt());

    if (command != null) 
        command.execute(game, view);
    else 
        view.showError(Messages.UNKNOWN_COMMAND);
}   
```

Basically, while the game is not finished (due to internal game reasons or to a user exit), the
program reads the text entered by the user, parses it to obtain an object implementing the `Command`
interface and then calls the `execute` method of this object to effect the functionality of the command
entered by the user and to update the view after doing so, if necessary. In the case where the
input text does not correspond to any of the existing commands, the error message
`Messages.UNKNOWN_COMMAND` is printed.

**Genericity of the `Controller` class**. The most important part of the code for the
main loop shown above is the following line assigning a value to the polymorphic variable
`command`:

```java
Command command = CommandGenerator.parse(...);
```
The key point is that the controller is generic: it simply knows how to handle objects that
have an `execute` method (it relies on the `CommandGenerator` invoking a `parse` method and,
for the `help` command, a `helpText` method on those same objects) 
but does not know which concrete command is being executed nor anything about the result of
that execution; the knowledge of what functionality corresponds to each
command is contained in each concrete command class. It is this mechanism that facilitates the
addition of new concrete commands with minimal changes to the existing code. 

**The `CommandGenerator` class**. The `parse` method of this class is a static method
which returns an instance of the concrete command class that corresponds to the text entered
by the user. To this end, the `CommandGenerator` class has a static attribute containing a list
of instances implementing the `Command` interface, concretely, it contains exactly one instance of 
each of the existing concrete command classes. The `parse` method of the `CommandGenerator` method
traverses this list calling the `parse` method of each object on it.
If any of these `parse` methods returns a non-null value (which will be one of the concrete
command classes), the `parse` of the `CommandGenerator` returns this non-null value,
otherwise it returns the value `null`.

The `CommandGenerator` contains another static method `commandHelp` that generates
the output of the help command (so must be called by the `execute` method of the `HelpCommand`
class). Like the `parse` command, it accomplishes its task by traversing the `AVAILABLE_COMMANDS`
list but in this case invoking the `helpText()` method of each object on the list.

The following is a skeleton of this code:

```java
public class CommandGenerator {

    private static final List<Command> AVAILABLE_COMMANDS = Arrays.asList(
        new UpdateCommand(),
        new ResetCommand(),
        new HelpCommand(),
        new ExitCommand(),
        // ...
    );

    public static Command parse(String[] commandWords){
	//...
    }

    public static String commandHelp(){
	//...
    }

}
```

<!-- TOC --><a name="herencia-y-polimorfismo"></a>
### The game-object inheritance hierarchy

We have seen that the use of inheritance in the `Command` inheritance hierarchy significantly reduces the
repetition of code. Moreover, the use of
inheritance and polymorphism in the *command pattern* greatly facilitates the introduction of new commands,
the key aspect being that the `Controller` class is generic, i.e. it does not handle specific commands
but only handles objects of the abstract class `Command` using polymorphic variables.

Similarly, use of inheritance in an inheritance hierarchy of game objects would also reduce the repetition
of code and facilitate the introduction of new game objects. The key aspect to obtaining the latter benefit
is that the `Game` and `GameObjectClasses` classes
be generic, i.e. they should not handle specific game objects only objects of an abstract class
called `GameObject`, from which all the concrete game object classes
(currently `Mario`, `Ground`, `Goomba` and `ExitDoor`) derive. So *the game code of the `game` and of the
`container` must **not** seek to
identify the  dynamic type (i.e. which concrete subclass of `GameObject`) of the objects it is
handling* [^7].

The `GameObject` class should contain
all the attributes and methods that are common to all the concrete game object classes; where appropriate,
each concrete game object class can overwrite inherited methods to implement its own behaviour. Note that

- in the `GameObject` class, the choice of whether to define an abstract method to be overwritten in all
  of the subclasses, or a method containing default behaviour to be overwritten only in those subclasses
  that do not implement this default behaviour, is a design decision that should be made according to which
  option produces the simplest code.
  
- attributes and non-abstract methods should always be placed in the highest class possible of the
  inheritance hierarchy [^8]

All game objects have, at least, an attribute to store the `game`, an attribute to store their position and a
boolean attribute to indicate whether they are alive or not. They have, at least, methods to manipulate their
position and a method to communicate whether they are alive or not. They will also have the method:

```public void update()```

which may have an empty body for simple classes such as the `Ground` class.

It is useful to factorise the behaviour and state of objects that can move (currently objects of the
`Mario` and `Goomba` classes) into an abstract subclass of `GameObject` called `MovingObject`. This class
contains (at least) an attribute to store the current direction of movement and a boolean attribute `isFalling`.
It also contains methods implementing basic movements to be used by its subclasses such as `Mario` and `Goomba`.
The classes `Ground` and `ExitDoor` inherit directly from `Gameobject`.

[^7]: **Design principles**: ensuring that `Game` and `GameObjectContainer` are high-level classes that do
not depend on any concrete implementation, is in accordance with the **D** of the **SOLID** design principles
(the ***D**ependency Inversion Principle*).

[^8]: **Design principles**: This is in accordance with the DRY (*Don't Repeat Yourself*) design principle. 

<!-- TOC --><a name="contenedor-de-objetos-del-juego"></a>
### Simplifying the game-object container

Having refactored the code for the commands and for the game objects, we now turn our attention to the
management of the game objects. As in the previous assignment, the game objects will be managed by the
`GameObjectContainer` class. However, instead of using multiple lists, we can take advantage of the
inheritance hierarchy of game objects to store them all on a single list of objects of type `GameObject`.
For simplicity, we use an `ArrayList` [^9] of elements of type `GameObject`:

```java
public class GameObjectContainer {

	private List<GameObject> gameObjects;

	public GameObjectContainer() {
		gameObjects = new ArrayList<>();
	}
    //...
}
```
Observe that, like the `Game` class, the `GameObjectContainer` class only deals with objects of the abstract
class `GameObject` so, like the game code, *the container code must **not** seek to identify the dynamic type
(i.e. which concrete subclass of `GameObject`) of the objects it is handling*. Finally, it is of great importance
that the implementation details of the `GameObjectContainer` be private so, for example, it should not export the
value of any of the attributes of the `ArrayList` class that it is using to store the game objects.
This information hiding enables the implementation of the container to be changed without affecting the
rest of the program code [^10]. 

[^9]: `ArrayList` is a class defined in the Java collections library. The classes of this library
make use of *parametric polymorphism*, known as *Java generics* in Java, first
used in functional programming in the mid-70s and introduced in Java in 2004 (with Java 5).
The polymorphism associated with inheritance is called *inclusion polymorphism* or *subtype polymorphism*.
Java generics will be studied in detail in the TP2 course.

[^10]: **Design principles**: This is in accordance with the design principle known as *Separation of Concerns* or,
more specific to classes in OOP, the *Single Responsibility Principle*, the **S** of the **SOLID** design
principles. As a *sanity check*, imagine, for example, that you decided to change the implementation of the
`GameObjectContainer` to store the game objects in a 2-dimensional array of objects that do not know
their own position, instead of using a list of objects that do know their own position. Would you need to
introduce any modifications in the code of the `Game` class of your implementation? The answer should be no.


<!-- TOC --><a name="generalizacion-interacciones"></a>
### Generalising the interactions between game objects

To model interactions between objects, such as those we have already seen between between `mario` and `exitDoor`
and between `mario` and `Goomba` objects, we will use the interface `GameItem` and a technique known as ***double-dispatch***,
in which the run-time selection of the method body to be executed (this being known as *dispatch*) depends on the dynamic class
of *both* of the objects involved in the interaction. This is implemented by having the dispatch depend not only on the dynamic
type of the object on which the method call was made, the usual dynamic-binding case, but also on the dynamic type of the object
that constitutes the method argument (or, more generally, on that of one of the method arguments, for double-dispatch, or on that
of several of the method arguments, for multi-dispatch).
In fact, unlike in some other languages, neither multiple dispatch, nor double dispatch is directly implemented in Java
but there are ways to simulate it. For simplicity,
we choose one that, strictly speaking, is not even double dispatch since the dispatch depends on the dynamic type of one of the
participants in the interaction and the static type of the other. Though this *"false" double dispatch* is simpler than other
possible Java solutions, it leads to undesireable repetition of code.

The `GameObject` class implements the `GameItem` interface in order for all game objects to have the possibility of
interacting with the other game objects via the methods of this interface (and *only* via the methods of this interface).

```java
public interface GameItem {
	public boolean isSolid();
	public boolean isAlive();
	public boolean isInPosition(Position pos);

	public boolean interactWith(GameItem other);

	public boolean receiveInteraction(Ground ground);
	public boolean receiveInteraction(Mario mario);
	public boolean receiveInteraction(ExitDoor door);
	public boolean receiveInteraction(Goomba goomba);
}
```

Note the overloading of the methods `receiveInteraction`. Note also that the interaction is not implemented symmetrically,
as indicated by the use of the word "receive" in the name of these methods, indicating that each
interaction is initiated by one of the objects involved. A valid implementation of the `interactWith` method is as follows:

```java
	  public boolean interactWith(GameItem other) {
	  	boolean canInteract = other.isInPosition(this.pos)
	  	if (canInteract) {
	  		...
			... other.receiveInteraction(this);
			...
		}
		return canInteract && ...
	  }
```

Each concrete subclass of `GameObject` must have an implementation of the `interactWith` method but it cannot obtain it
by inheritance and dynamic binding, i.e. by simply using the above method code as that of a `default` method in the
`GameItem` interface (or by simply placing this code in the `GameObject` class). This is because, though a call to a
method of the object referenced by `this` in an interface (e.g. `this.toString()`) will
use normal dynamic binding and
will work correctly (i.e. will use the method body of the dynamic type of the object implementing the interface), the same is not true for choosing the method body in
function of the argument, here of the `receiveInteraction` method, 
because method overloading is resolved at compile time. Here, this means that in the
argument of the call to `receiveInteraction` appearing in the body of the `interactWith`
method, the method body used will be that of the static type of the
object referenced by `this`. In consequence, in this *"false" double dispatch*, the code
for the `interactWith` method has to be copied into each of the concrete subclasses of
`GameObject` in order for the static type in question to correspond to the concrete type
of the object whose `interactWith` method has been invoked [^11].

We then extend this way of coding the interaction between game objects to the container by adding a method in the
`GameObjectContainer` class that carries out all the interactions of a given object (provided as argument) with the
other objects that it manages (both `other.interactWith(this)` and `this.interactWith(other)` must be checked).

```java
	  public boolean doInteraction(GameItem obj) {...}
```

A boolean return value can be used to indicate whether any object has been modified. Finally, in order for other
game objects to be able to generate interactions with their environment, a method calling this container method should be added
to the `Game` class and declared in the `GameWorld` interface, see the next section.

[^11]: Strictly speaking, the call to `receiveInteraction(this)` is also breaking
encapsulation but this is an unavoidable consequence of trying to implement
double-dispatch in Java in a relatively simple way.

<!-- TOC --><a name="interfaces-de-game"></a>
### Interfaces implemented by the `Game` class

The `Game` class offers services to different parts of the program, namely:

- *Controller*: invokes those methods of the game, such as `update` or `reset`, that implement the commands entered by the user; after the above-described refactoring, the calls to these methods are made from the body of the `execute` methods of the `Command` classes. It also invokes methods that return information about the state of the game, such as `isFinished`.

- *View*: invokes methods that return information about the state of the game, such as `getPoints`, `playerWins` o `positionToString`, that is needed to display the current state.

- *Model*: the game objects (part of the *Model*, as is the `Game` class itself) invoke those methods of the game, such as `isSolid(Position)`, `addPoints(int)` or `marioArrived()`, that concern interactions between game objects. Since these calls *to* the game from the game objects result from calls *by* the game to the game objects (via the container, usually as part of an update), they are referred to as *callbacks*.

Notice that with the current implementation, nothing prevents the *Model* invoking a method of `Game` that was designed for the *Controller* to invoke, e.g. a game object invoking the `reset` method, or the *View* invoking a method of `Game` designed for the *Model* to invoke, e.g. the game view invoking the `marioArrived` method, etc. In order for the compiler to detect such inconsistent invocations we can use interfaces to define *partial views* on the services offered by the `Game` class. To that end, we define the following three interfaces [^12]:

- `GameWorld` to represent the *Model*'s internal view of the services offered by the `Game` class.

- `GameStatus` to represent the *View*'s view of the services offered by the `Game` class,

- `GameModel` to represent the *Controller*'s view of the services offered by the `Game` class,

For example:


```java
public interface GameModel {

	public boolean isFinished();
	public void update();
	public void reset();
	// ...
}
```

The `Game` class must then implement these interfaces:

```java
public class Game implements GameModel, GameStatus, GameWorld {

	// ... 
	private GameObjectContainer container;
	private int nLevel;
	// ...
	
	// Methods declared in GameModel
	// ...
	// Methods declared in GameWorld
	// ...
	// Methods declared in GameStatus
	// ...
	// Other methods
	// ...
}
```

Finally, in each of the three parts of the program, we must replace each occurrence of the type `Game` by the corresponding interface type. For example, the execute method of the `Command` class now has the following form:

```java
public abstract void execute(GameModel game, GameView view);
```

[^12]: **Design principles**: This is in accordance with the **I** of the **SOLID** design principles (the *Interface Segregation Principle*).

<!-- TOC --><a name="pruebas"></a>
## Testing

Recall that after refactoring, the program should have exactly the same functionality as the versión previous to the refactoring (though some
error messages may need to be less precise) and should therefore pass the same system tests, even though the implementation now contains many more classes.

The template that we provide you with includes classes called `tp1.Tests_V2_1` and `tp1.Tests_V2_1` which, like `tp1.Tests`, are classes of JUnit tests, the former containing the test cases for part I of this assignment and the latter containing test cases for the extensions implemented in part II of this assignment.



















