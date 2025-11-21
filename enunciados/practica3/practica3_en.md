<!-- TOC start -->
- [Assignment 3: Exception handling and file handling](#practica-3-excepcionesYficheros)
- [Introduction](#introduccion)
- [Exception handling](#exceptions)
	- [Exceptions thrown in the *Control* part of the program](#command-exceptions)
	- [Exceptions thrown in the *Model* part of the program](#gamemodel-exceptions)
- [File handling](#files)
	- [Serialization / deserialization](#serialization)
	- [Saving the game state to file: the `save` command](#save-command)
	- [Loading the game state from file: the `load` command](#load-command)
	- [Adapting the `reset` method of the `Game` class](#reset-load-game)
	- [Initial configurations of the game (optional)](#level-conf)
<!-- TOC end -->

<!-- TOC --><a name="practica-3-excepcionesYficheros"></a>
# Assignment 3: Exception-handling and file handling

**Submission: December 1st 2024, 12:00**

**Objectives:** handling exceptions and files

<!--
**Preguntas Frecuentes**: Como es habitual que tengáis dudas (es normal) las iremos recopilando en este [documento de preguntas frecuentes](../faq.md). Para saber los últimos cambios que se han introducido [puedes consultar la historia del documento](https://github.com/informaticaucm-TPI/2425-Lemmings/commits/main/enunciados/faq.md).
-->

<!-- TOC --><a name="introduccion"></a>
# Introduction

In this assignment we extend the functionality of the Mario game of the previous
assignment in two ways:

- *Exception handling*: errors that may occur during the execution of the application
can be more effectively dealt with using the exception mechanism of the Java language.
An exception object encapsulating any information about an error and its context that
is considered to be of interest, in particular, an error message, is created at the
point in the code where the error occurs and is then passed between the methods of
the call stack. As well as making the program more robust, this mechanism enables the
user to be informed about the occurrence of an error in whatever level of detail is
considered appropriate, while at the same time providing a great deal of flexibility
in regard to where the error is handled (in particular, to print an error message).

- *File handling*: a useful addition to the application is the facility to save the
state of a game to file and load the state of a game from file. The loading of the
initial state will than be a particular case of this general mechanism. To this end,
we add two new commands, one to write to a file and the other to read from a file.
The use of the *command pattern* introduced in the previous assignment greatly
facilitates the addition of new commands.
  
<!-- TOC --><a name="exceptions"></a>
# Exception handling

In this section, we present the exceptions that should be handled by the application and
give some information about their implementation. The section is divided into two parts,
the first dealing with exceptions that may be thrown in the control part of the
application, in particular, in the parsing of the user input, the second dealing with
exceptions that may be thrown in the model part of the application, in particular, in
the implementation of the execution of the commands.

It should be pointed out that file handling inevitably involves exception handling,
particularly when reading data from files, which is why these two topics are often
introduced to students at the same time. Exceptions that may be thrown during file handling
(i.e. during the execution of the `save` and `load` commands, or during the loading of the
initial state) will be dealt with in the file-handling part of this document.

You will have observed that there are circumstances in which a
command may fail, either in its parsing or in its execution. For example, the parsing
of the `action` command will fail if the user provides a parameter that cannot be
parsed as a valid action. In the previous assignment, on occurrence of such errors,
the `parse` method of the `Action` class and that of the `CommandGenerator` class simply
returned `null`. Returning `null` in case of error does not transmit any indication about
the reason for the occurrence of the error, nor does it permit the transmission of any
other data about the error that may be required to handle it correctly.
As another example, the execution of the `addObject` command will also fail if the
position of the object given by the user is outside the board or contains a solid object.
In the previous assignment, the occurrence of such an error was communicated to
the `execute` method of the `addObject` class via a boolean return value (an alternative
could be to use some ad-hoc mechanism involving specific methods in the `Controller`
class but, apart from the problem of being ad-hoc, this would oblige the game to know
about the controller, which we seek to avoid).
Returning the value `false` to communicate the occurrence of an error has similar
problems to returning `null` for this same purpose.

In this assignment, we deal with these issues using the Java exception mechanism. An
exception mechanism provides a flexible communication channel between the location in
the code where an error occurs and the location in the code where that error is handled,
along which any required data concerning the occurrence of the error can be sent from
the former to the latter[^1]. In many cases, the data concerning the occurrence of the error
that is transmitted from one code location to another via an exception consists simply of
an error message (as well as the stack trace which the system includes in any exception),
and the handling of this error consists simply of sending that message
to the standard error to be displayed on the screen. In the general case, however, more
data about the error and its context may be transmitted between code locations and the
error-handling may require more complex actions than simply printing a message to the
screen. In this application, the exception mechanism enables us to ensure that
the controller is responsible for sending all error messages to the view to be displayed.

[^1]: The error code mechanism of C is somewhat primitive in comparison, though it is also much
less computationally costly, which is why C++ retains it as well as having an exception mechanism
(though this exception mechanism is less type-checked and more difficult to use than the Java
equivalent)

For simplicity, we recommend that you place all programmer-defined exception classes
in a package called `tp1.exceptions`.


<!-- TOC --><a name="command-exceptions"></a>
## Exceptions thrown in the *Control* part of the program

We define a new exception class called `CommandException` and two subclasses:

- `CommandParseException`: used for exceptions that occur during the parsing of a command
  (so during the execution of the `parse` method of the `Command` class)
	
- `CommandExecuteException`: used for exceptions that occur during the execution of a command
  (so during the execution of the `execute` method of the `Command` class)

### Parsing errors

First we deal with the occurrence of errors during the parsing of the commands, which will lead
to the throwing of a `CommandParseException`, instead of returning `null` as was done in the previous
assignment. 

The `parse` method of the `Command` class must now be declared to throw exceptions of type 
`CommandParseException`.

```java
  public abstract Command parse(String[] parameter) throws CommandParseException;
```
For example, the `parse()` method of the `NoParamsCommand` can now be implemented as follows:

```java
  public Command parse(String[] commandWords) throws CommandParseException {
    // in fact, commandWords.length == 0 not possible due to strip() in getPrompt() method
    if commandWords.length != 0 && matchCommandName(commandWords[0]))
      if (commandWords.length > 1)      // there are extraneous parameters
        throw new CommandParseException(Messages.COMMAND_INCORRECT_PARAMETER_NUMBER);
      else
        return this
    return null
  }
```

Note that the `parse` method of the commands must only throw an exception if the first word
of the input data matches the command name and there is an error in the command
arguments. Not matching the command name is not an error but simply an indication that
the input text does not correspond to this particular command so, in this case, the `parse` method
must not throw an exception and must return `null` as it did in the previous assignment,
to enable the `parse` methods of the other commands to also check for a match.

The `parse` method of the `CommandGenerator` class must now also be declared to throw exceptions of
type  `CommandParseException` in order to transmit to the controller the exceptions thrown by the
commands:

```java
	public static Command parse(String[] commandWords) throws CommandParseException
```

but also to transmit exceptions of type `CommandParseException` thrown by itself, in the case
where none of the `parse` methods of the commands recognises the first word of the
input data, i.e. the case of an unknown command (instead of returning `null` and having the
controller handle this case with an *if-then-else*):

```java
	throw new CommandParseException(Messages.UNKNOWN_COMMAND.formatted(commandWords[0]));
```

The `NumberFormatException` is a system exception that is thrown when an attempt is made to parse
the `String`-representation of a number and convert it to the corresponding value of type
`int` or `long` or `float`, etc., in the case where the input string does not, in fact, represent
a number and cannot, therefore, be so converted. A `NumberFormatException` should be caught
in the `parse` method of the command where it occurs and wrapped in a `CommandParseException`.
The term "wrapping" refers
to the good practice of catching a lower-level exception and throwing a higher-level exception
which includes it (accomplished by passing the lower-level exception as argument to the constructor
of the higher-level exception) and *which contains a less specific message than the low-level one*[^2].

[^2]: You will probably have seen examples of bad practice in this regard, e.g. electronic commerce web
applications that produce messages for the user of the type "SQLException...". The user does not care about
this level of implementation detail and should not be receiving this type of message.

For example, in the `parse` method of the `ResetCommand` class with a level argument you should do
something similar to the following:

```java
	} catch (NumberFormatException nfe) {
		throw new CommandParseException(
			Messages.LEVEL_NOT_A_NUMBER_ERROR.formatted(commandWords[1]), nfe);
	}
```

We also define the following new Exception classes for exceptions which may be thrown when parsing
text that is supposed to represent an action or when parsing text that is supposed to represent a
position:

  - `ActionParseException`: to be thrown by the `Action` class when an attempt is made to parse
     a string that does not correspond to any of the literals of the enum.
	
  - `PositionParseException`: to be thrown by the `Position` class when an attempt is made to
     parse a string that is not the textual representation of a position string, i.e. does not
     respect the syntax for a position string (note that this does not concern whether or not
     the position is off the board)
     
If thrown during the parsing of a command, these exceptions should also be caught and wrapped
in a `CommandParseException`.


### Execution errors

An error that occurs during the execution of a command will lead to the throwing of a
`CommandExecuteException` in the body of the `execute` method of the commands. This exception will
be used to wrap lower-level exceptions thrown on the occurrence of errors in the model
part of the application (we deal with these errors in detail in the next section).

The `execute()` method of the `Command` class must now be declared to throw exceptions of type 
`CommandExecuteException`:

```java
	public abstract void execute(GameModel game, GameView view) throws CommandExecuteException;
```

### Capturing parsing errors and execution errors in the controller

As already stated, both types of exceptions, `CommandParseException`s and `CommandExecuteException`s,
reach the controller, which is responsible for catching them and sending the 
messages they contain to the view for display. In the case where an exception wraps a lower-level
exception (which may, in turn, wrap an even lower level exception etc.), in our application,
the controller sends all levels of error messages to the view for display. 

The `run()` method of the controller will now contain the following code. Notice that to avoid
extra lines of code, we are using the fact that in Java, the assignment operator evaluates to the
value being assigned (as it does in C).


```java
	public void run() {
		...
		while (!game.isFinished()) {
			...
			try { ... }
			catch (CommandException e) {
				view.showError(e.getMessage());
				Thowable wrapped = e;
				// display all levels of error message
				while ( (wrapped = wrapped.getCause()) != null )
					view.showError(wrapped.getMessage());
			}
		}
		...
	}
```

In a real application, of course, a normal user would
almost certainly not be interested in seeing the messages from all the different levels (see the
above footnote about the bad practice in many electronic commerce applications of displaying
*SQLExceptions* to normal users), though an administrator may be, and comprehensive information
about errors is also what is required if it is to be written to an error log.

Finally, notice that, usually, both the parsing and the execution of commands with parameters 
throw more types of exception than do the parsing and execution of commands without parameters.
In the case of the `addObject` command, for example, the parsing of its parameters may generate
`CommandParseException`s, or system exceptions that we wrap in `CommandParseException`s, and its
execution in the model part of the application may generate exceptions that we wrap in
`CommandExecuteException`s.

<!-- TOC --><a name="gamemodel-exceptions"></a>
## Exceptions thrown in the *Model* part of the program

As stated above, the errors in the execution of commands arise in the game logic, that is, in the
model part of the program and, in the previous assignment, the methods involved returned a `boolean`
value to indicate whether the execution had succeeded or failed. For example, the  `addObject` method
of `GameModel` returned `false` when the position passed as argument was outside the board or when
the parameter does not correspond to any known game object. However, returning the value `false` did
not permit these two types of error to be distinguished. Generating error data, in particular an
error message, at the point in the code where the error occurs enables such distinction. We could
repurpose the boolean return value and maintain it as an indication of whether or not the game state
has been modified. The program con now display the following error messages (notice the two messages,
one from each level of exception):

- On trying to add an unknown type of game object:
	```
	[DEBUG] Executing: addObject (3,2) poTaTo

 	[ERROR] Error: Command execute problem
 	[ERROR] Error: Unknown game object: "(3,2) poTaTo"
	```
  We define a new exception `ObjectParseException` to be thrown when an attempt is made to
  parse a string, the syntax of the object part of which is incorrect, or whose object part
  is not the external representation of any known game object.
  
  excepción lanzada cuando no se puede analizar la línea porque su formato es incorrecto, por ejemplo por no tener todos los datos necesarios, por tener más datos de los necesarios, por tener un nombre de objeto desconocido, etc.

- On trying to add an object in a position that is outside the board:
	```
	[DEBUG] Executing: addObject (-4,24) Ground

	[ERROR] Error: Command execute problem
	[ERROR] Error: Object position is off board: "(-4,24) Ground"
	```
  We define a new exception `OffBoardException` to be thrown when an attempt is made to access
  a position that is outside the board.
  
For convenience, we also define a new exception `GameModelException` which is the superclass of
the above two exceptions. The method `addObject` method of the `GameModel` interface is now declared
to throw (at least) these two exceptions:

```java
public void addObject(String[] objWords) throws OffBoardException, ObjectParseException;
```

or, alternatively,:

```java
public void addObject(String[] objWords) throws GameModelException;
```

If, in your solution, the parsing of the position part of the argument to the `addObject`
command is carried out in a method of the `Game` class, the `addObject` method will also
need to declare the throwing of `PositionParseException` as defined above.
If, in your solution, the action literals `LEFT` and `RIGHT` are also used to represent
movement directions, the `addObject` method will also need to declare the throwing of
`ActionParseException` as defined above.

The above exceptions are to be thrown by methods of the `GameModel` interface that are
called from one or more of the `execute` methods of the commands. As already stated, they
should be caught in the corresponding `execute` method and rethrown, wrapped in a
`CommandExecuteException`. For example:

```java
} catch (OffBoardException obe) {
	throw new CommandExecuteException(Messages.ERROR_COMMAND_EXECUTE, obe);
}
```

With this procedure, no information is lost since the wrapped exception can be recovered
using the `getCause` method of the `Throwable` class (see the above code for the `run`
method of the controller).


<!-- TOC --><a name="files"></a>
# File-handling

<!-- TOC --><a name="serialization"></a>
## Serialization / deserialization

In computing, the term *serialization* refers to converting computing structures into a stream
of bytes usually with the objective of saving this stream to a file or transmitting it on a network.
Commonly, the structure being serialized is the current state of an executing program, or of part
of an executing program. The term *deserialization*
refers to the inverse process of reconstructing the computing structures, commonly the state of
an executing program, or part of an executing program, from a stream of bytes.
Serialization/deserialization in which the
generated stream is a text stream is sometimes referred to as stringification/destringification.
Clearly, the format used for serialization/stringification should be designed in such a way
as to facilitate deserialization/destringification.

Java incorporates a generic serialization mechanism capable of converting any Java objects,
and therefore any executing Java program or part of any executing Java program,
into a binary stream (see classes `ObjectInputStream` and `ObjectOutputStream`).
We do not require such a generic serialization/deserialization
mechanism; our interest is simply to serialize to, and deserialize from, a text stream
that represents the current state of our game, with a view
to writing this state to, and reading this state from, a text file. Note that the textual
representation currently produced by the view is not a suitable format for serialization since
deserialization of this format would be a rather complicated enterprise. We therefore
need to define a text-serialized format, in which the state of the game is represented as
the global values followed by a sequence of game elements, each of which is represented as
a sequence of words in a specified format. For this specified format, we can use the external
representation introduced in the previous assignment to be used with the `addObject` command
(as the format for its argument). To enhace the readability of the serialized content, we
separate the external representation of each game object with a line break.

### Serialization format

We explain our text-based serialization format via a simple example. The following represents
a game comprising two grounds, two goombas, one box, mario and an exit door:
```
100 0 5
(14,0) Ground
(14,1) Ground
(14,2) ExitDoor
(11,1) Box Empty
(13,0) Mario RIGHT Small
(13,1) Goomba RIGHT
(13,1) Goomba LEFT
```

The numbers on the first line represent, from left to right:

- the remaining time
- the points
- the remaining lives

Each of the following lines represents the state of a game object, according to the external
representation format described in part II of the Assignment 2 problem statement.

<!-- TOC --><a name="save-command"></a>
## Saving the game state to file: the `save` command

Having defined our serialization format, in fact a stringification format, the next logical
step is to introduce a new command, the `save` command, which can be used to save the
serialized current state of the game in a file. In accordance with OOP principles, each class
should produce its own "stringification".

Recall that the purpose of the Java `toString()` method is to return a human-readable textual
representation of the owning object. Since our serialization is to be stringification, we
can use the `toString` method of the game objects for this purpose. To this end, we introduce
a new method in the `Game` class [^3]:
```java
	public void save(String fileName) throws GameSaveException {...}
```
This method, to be called by the `execute` method of the `SaveCommand` class, can be implemented
by simply calling the `toString` method of the `game` which calls the `toString` method of the
`container`, which calls the `toString` method of each of the game objects and then writes
them to file [^4]. Exceptions occuring during the execution of the `save` method should be
caught and wrapped in a `GameSaveException`, a subclass of `GameModelException` (which must then
be caught and wrapped in a `CommandExecuteException`) [^5].

The output of the `help` command should now include details about the `save` command:
```
Command > h
[DEBUG] Executing: h

Available commands:
   ...
   [s]ave <fileName>: save the actual configuration in text file <fileName>
   ...
```
Regarding the `parse` method of the `SaveCommand` class, the simplest way to
implement a `save` command would be without parameters, the name of the file to be
used being ``hardwired'', i.e. stored in a constant attribute of the `SaveCommand`
class, allowing only one game state to be saved at any one time. Alternatively,
the `save` command could be implemented with one `String` parameter: the file name.
As can be seen from the output of the `help` command, in this assignment you must
implement the `save` command with a file-name parameter, so the `parse` method of
`SaveCommand` will have to deal with this parameter.

[^3]: Which class should be responsible for opening the input character stream is a
not-so-obvious design decision.
Instead of the `execute` method of the `SaveCommand` class passing the filename as an
argument to the `save` method for it to then open the input character stream as
suggested here, the `execute` method could open the input character stream and then
pass it as an argument to the `save` method, rather than passing the file name.

[^4]: It could be argued that the textual representation being saved to file is a view in
the sense of the MVC architecture and that the *View* part of the application should
therefore be reponsible for generating it. This could easily be accomplished by simply
moving the `save` method from the `game` to the view, so that the view is responsible for
calling the `toString` method of the `game`. However, as a counterargument, should a binary
serialization, rather than a "stringification", also be considered a view?

[^5]: Though not stated in the Java specification, the usual policy is that when a program
attempts to open an output stream to a file that does not exist, it is created (it is
stated in the Java specification that if the file already exists, it is overwritten, unless
opened in *append* mode). However, a `FileNotFoundException` may still be thrown on trying
to open an output stream since it is thrown in cases other than the absence of a
file with that name, e.g. if the filename cannot be correctly resolved (see the Java
documentation).

<!-- TOC --><a name="load-command"></a>
## Loading the game state from file: the `load` command

In the following sections we present an implementation of a new command, the `load` command,
used to read a state of the game from a file. The implementation of the `load` command
is inevitably more complicated than that of the corresponding `save` command though we
have already defined the serialization format and, moreover, we have already created the
code that parses this format, in particular, the object factory. We divide the
task of implementing the `load` command into the following subtasks:

- [add exceptions to the `GameObjectFactory` class](#game-object-factory),

- [define a `GameConfiguration` interface](#game-configuration) to be implemented by classes
  representing a game configuration, i.e a valid state of the Mario game,

- [create a `FileGameConfiguration` class](#game-configuration-loader), responsible for
  loading the serialized data from file and storing it in a game configuration,
  delegating part of the task to the game object factory.

- [add the implementation of the load command to the `Game` class](#game-load),

- [create the `LoadCommand` class](#load-command-class).

On finishing these changes, we will need to check the exceptions that may be thrown on
loading a file, though, regarding errors in the content of the file, we recommend first
developing the code under the assumption that the file contains no format errors and then
modifying it to add the handling of the exceptions that may be produced if it does have
such errors.

Note that the behaviour of the `reset` command will need to be adapted to the existence
of the `load` command since reset should mean return to the last loaded state.


<!-- inner TOC --><a name="game-object-factory"></a>
### Exceptions in the game object factory

As we did for the command factory (i.e. the `CommandGenerator` class), we must define
the exceptions to be thrown by the `parse` method of the game-object factory to avoid
returning `null`. These are `ObjectParseException` and `OffBoardException`, the two
subclasses of `GameModelException` defined in a previous section.
	
Observe that, as is the case for the `CommandGenerator`, the game-object factory never
returns `null`; either the parsing is successful or an exception is thrown [^6].

Note that if your implementation uses literals of the `Action` class to represent movement
directions, the `parse` method of the game-object factory may also throw `ActionParseException`.
Note also that the parsing of the position part of the external representation, though not
carried out in the game-object factory, may throw `PositionParseException`.

[^6]: This is in accordance with the behaviour of library methods such as [Integer.valueOf](https://docs.oracle.com/javase/8/docs/api/java/lang/Integer.html#valueOf-java.lang.String- "Integer.valueOf"). 

<!-- inner TOC --><a name="game-configuration"></a>
### Representing the loaded state of the game: the `GameConfiguration` interface

A game state can be encapsulated in an object that stores the different components of such
a state, namely the value of the `game` attributes, remaining time, points and remaining lives,
and the set of game objects, where we will
assume that the latter is contained in a newly-created object of the `GameObjectContainer` class.
Any object representing a game state must provide methods to access the different components
of this state, i.e. it must implement an interface containing the following method declarations:
 ```java
	// game status
	public int getRemainingTime();
	public int points();
	public int numLives();
	// game objects
	public Mario getMario();
	public List<GameObject> getNPCObjects();
 ```
Here we are assuming that `game` has an attribute in which it stores a reference to
`mario`, obliging the interface to distinguish `mario` from the other
game objects (the NPCs). If, in your implementation, the `Game` class does not have
an attribute of type `Mario`, the last two methods can be replaced with
the method:
```java
 	public List<GameObject> getGameObjects();
```

We will define this collection of method declarations to be those of a
`GameConfiguration` interface, to be placed in the `tp1.logic` package.
If you wish to avoid repeating code, you could define an interface called,
say, `GameRunStatus` containing the first three of the above methods
and then inherit the `GameRunStatus` interface in both the `GameConfiguration`
interface and the `GameStatus` interface.


<!-- inner TOC --><a name="game-configuration-loader"></a>
### Reading the file data: the `FileGameConfiguration` class

As a general principle, reading from a file must never:
1. cause a program to crash,
2. leave it in an incoherent state
   
How do we accomplish this?
1. Catching all exceptions that could be thrown when reading from a file ensures that
  the program cannot crash. If the load fails, the program can handle the exception,
  informing the user, and continue the program exactly as if the load had not been
  attempted. To this end, to be sure that no exception will go uncaught, after catching
  specific exceptions, your code should include a clause catching a general exception,
  `} catch Exception e {`, wrapping it, as for the other exceptions thrown during
  loading, in a `GameLoadException,` see below.
2. Loading the file data into a special-purpose object which is then only used (in our
  case as the new state of the game) if and when the data has been completely and
  successfully loaded from file is one way of ensuring that the program cannot be left
  in an incoherent state. In our case, this special-purpose object is an instance of the
  `FileGameConfiguration` class which implements the `GameConfiguration` interface and
  is to be placed in the `tp1.logic package`. Ensuring that an incoherent game state
  can never be used as a working game state is facilitated by performing the loading
  from file in the constructor of the `FileGameConfiguration` class. Thus, if the
  checking of validity is exhaustive, objects of this class that encapsulate an
  incoherent game state can never exist.

The `FileGameConfiguration` constructor has two parameters:
- a `String` containing the name of the file from which the game state is to be loaded [^7].
- the game, typed as `GameWorld`, to be passed to the constructor of the game objects
  (via the parse method of each game object, via the parse method of `GameObjectFactory`).

```java
public FileGameConfiguration(String fileName, GameWorld game) throws GameLoadException;
```

[^7]: The design decision discussed in a previous footnote also affects this
constructor. We could have used an input character stream as the parameter instead of
the file name string, in which case, the `game` would be responsible for opening this
input character stream. This alternative is more flexible since it allows the source
of the input character stream to be a file or something else (though it should perhaps
also involve changing the name of the class from `FileGameConfiguration` to, say,
`GameConfigurationImpl` to reflect this generalisation).

The constructor can throw the following programmer-defined exception:

- `GameLoadException` (a subclass of `GameModelException`): used to wrap any exception
  thrown during loading, such as `FileNotFoundException`, any operating system exception,
  `ObjectParseException`, `OffBoardException`, `PositionParseEsception` or any other
  exception thrown due to an incorrect file format [^8].

[^8]: If you wish to be exhaustive about this, the validation should also check conditions
such as that the remaining time, points and remaining lives are within the accepted range
for these values, and raise an exception if not. However, this is not obligatory.

<!-- inner TOC --><a name="game-load"></a>
### Adding the implementation of the load command to the `Game` class

We need to add the possibility of loading the state of the game from file to the model
part of the application. To this end, we add a function called `load` to the `Game` class
(we will also need to add it to the corresponding interface): 

```java
	public void load(String fileName) throws GameLoadException {...}
```

This method simply creates a `FileGameConfiguration` object, typed as a `GameConfiguration`
and then sets the attributes of the game to the values returned by calls to the methods of the
`GameConfiguration` interface. It must declare that it throws the same exceptions as the
constructor of the `FileGameConfiguration` class.


<!-- inner TOC --><a name="load-command-class"></a>
### The `LoadCommand` class

We can now implement the `LoadCommand` class to enable the player to ask the game to
load a game state from file. After successfully loading such a game state, the `execute`
method of this class must display it.

The output of the `help` command should now include details about the `save` command:
```
Command > h
[DEBUG] Executing: h

Available commands:
   ...
   [l]oad <fileName>: load the game configuration from text file <fileName>
   ...
```

Regarding the `parse` method of the `LoadCommand` class, as for the `save`
command, you must implement the `load` command with a file-name parameter so
the `parse` method of `LoadCommand` will have to deal with this parameter.

The help message of the `load` command will have the following form:

```
[DEBUG] Executing: help

Available commands: 
   ...
   [l]oad <fileName>: load a state of the game from the text file <fileName>
   ...
```
<!-- TOC --><a name="reset-load-game"></a>
## Adapting the `reset` method of the `Game` class

Resetting a game that has been loaded from file using the no-argument reset should place the it
in the state it was in immediately after the last loading took place. This can be accomplished by
having the `load` method of the `Game` class  store the `FileGameConfiguration` object created
during loading in an attribute of the `Game` class, e.g.:

```java
private GameConfiguration fileloader;
```

If you carry out the optional task of storing the initial configurations in the serialized format
instead of using `initLevelX` methods (see the next section), the `fileLoader`attribute will
receive a value when the constructor of the game is initialised. Otherwise,
the value of the `fileLoader` attribute being `null` serves to indicate that the reset should
return the game to the initial state.

Be sure to check that the new implementation of the `reset` command works corectly, since you may
have encapsulation problems in the `FileGameConfiguration` class which prevent it from doing so.















