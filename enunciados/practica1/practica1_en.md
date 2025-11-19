# Assignment 1: Mario Bros (simplified version)

**Submission: 13th of october at 09:00**
 
**Objectives:** Introduction to object orientation and to Java; use of arrays and enumerations;
string handling with the `String` class; input-output on the console.

<!--
**FAQ**: Como es habitual (y normal) que tengáis dudas, las iremos recopilando en este [documento de preguntas frecuentes](../faq.md). Para saber los últimos cambios que se han introducido [puedes consultar la historia del documento](https://github.com/informaticaucm-TPI/2425-Lemmings/commits/main/enunciados/faq.md).
-->

<!-- TOC start -->
- [Plagiarism](#plagiarism)
- [1. Description of the assignment](#1-description)
  * [1.1 Introduction](#11-introduction)
  * [1.2. Details](#12-details)
- [2. Structure of the project](#2-project-structure)
- [3. Implementation (in stages)](#3-stages)
  * [3.1 Displaying the board](#31-display)
  * [3.2 Basic commands and the main loop](#32-main-loop-basic-commands)
  * [3.3 Automatic movement and updating the state of the game](#33-automatic-movement-and-update)
  * [3.4 Mario actions](#34-mario-actions)
  * [3.5 Collisions Mario ↔ ExitDoor](#35-collisions-mario-exitdoor)
  * [3.6 Collisions Mario ↔ Goombas](#36-collisions-mario-goombas)
- [4. Submission](#4-submission)
- [5. Testing](#5-testing)
  * [JUnit](51-JUnit)
<!-- TOC end -->

<!-- TOC --><a name="plagiarism"></a>
## Plagiarism

For each of the TP assigmments, all the submissions from all the different TP groups will be checked using anti-plagiarism software, firstly by comparing them all pairwise and secondly, by searching to see if any of their code is copied from other sources on the Internet. Any plagiarism detected will be reported to the *Comité de Actuación ante Copias* which, after interviewing the student or students in question, will decide whether further action is appropriate. This may include the opening of disciplinary proceedings with the relevant university authority (*Inspección de Servicios*).

<!-- TOC --><a name="1-description"></a>
# 1. Description of the assignment

<!-- TOC --><a name="11-introduction"></a>
## 1.1 Introduction

Super Mario is a classic videogame saga released at the beginning of the 1980s that had a big influence on general videogame development. The game consists of controlling an italian plumber called Mario as he traverses different worlds, avoiding enemies and obstacles, and rescuing the princess Peach from the villain Bowser.

We will implement a simplified version of **Mario Bros**, the first simplification being that we will use a text-based interface, rather than a GUI, where the world is represented as a 2D board on which Mario moves and encounters enemies. The second simplfication is that the original game evolves in real time, i.e. the elements of the game other than Mario move constantly, independently of any actions that the player may take, whereas the game we develop here evolves in discrete cycles. On each cycle, the game stops and waits for the player to input a command via the keyboard, after which the state of the game (including Mario) is updated taking into account the chosen command. The updating causes the elements of the game to carry out the appropriate movements and actions. In the course of the game, Mario can die, gain points and reach an exit door, which is the main objective of the player. 

![View of a modern version of the game](imgs/supermario-1.webp)

If you do not know the game or have never played it, we recommend that you try it before carrying out the assignment. Several free versions can be found on the web, one of them being available at the following URL: https://supermarioplay.com/.

During the term, we will progressively develop our own version of the game. In this first assignment, we develop a very reduced version in which Mario simply walks and carries out a few other actions and in which there is only one type of enemy, the Goomba.

<img src="imgs/goomba1.jpg" alt="A goomba" width="200">

In the next assignment we will incorporate more functionality, in particular, introducing more game elements and more commands, by using the tools of OOP, notably inheritance and polymorphism.

<!-- TOC --><a name="12-details"></a>
## 1.2. Details

In this version of the game, the world consists of a **15 x 30** board of cells (i.e. 15 rows by 30 columns). Of course, the size of the board should be implemented using constants so that it can easily be changed if required. The coordinates of the uppermost, leftmost cell are (0,0) and those of the lowermost, rightmost cell (14,29). In this version of the game, there are four game elements on the board: **Mario**, **Goomba**, **Ground** y **ExitDoor**. Their behaviour will be described in Section 3 of this document.

In each cycle of the game, the following actions are carried out sequentially:

1. ***Draw.*** The current state of the game is displayed.

2. ***User command.*** The user is prompted for a command; when the user enters a command terminated by a return, the program reads the command.

3. ***Update***. The command is executed and, if the command requires (some commands change the state of the game while others do not), the game is updated by updating all the elements of the game that are on the board.

The implementation of the game life-cycle is termed the *main loop* of the game. More detail concerning the main loop of the game can be found in Section 3 of this document.

<!-- TOC --><a name="2-project-structure"></a>
## 2. Structure of the project

You are provided with skeleton code comprising the following packages and classes:

+ `tp1`
    - _`Main`_
+ `tp1.view`
	- _`ConsoleColorsAnsiCodes`_
	- _`ConsoleColorsView`_
	- _`ConsoleView`_
	- _`ViewInterface`_
	- _`GameView`_
	- `Messages`
+ `tp1.control`
    - `Controller`
+ `tp1.utils`
    - _`MyStringUtils`_
+ `tp1.logic`
    - `Game`
    - `GameObjectContainer`
	- `Action`
	- `Position`
+ `tp1.logic.gameobjects`
    - `Mario` 
+ _`Tests`_

Each package contains several files. The files whose name is shown in italics contain completely implemented classes; you should not change the code contained in any of these files. The other files contain partially implemented classes, to which you will need to add more attributes and methods. You may also need to create additional classes and packages. The application is launched by executing the `tp1.Main` class.

The package structure of the code provided conforms to the Model-View-Controller (MVC) architectural pattern:

+ The **Model**  component of an MVC application contains the logic. In our case, the *Model* contains the rules of the game, the handling of the different elements of the game and, in general, everything concerned with the game such as remaining cycles, remaining lives of Mario, whether or not the game has finished, whether or not the player has won, etc. Correspondingly, in our package structure, the `logic` package contains the `Game` class, the `GameObjectContainer` class and other classes. The `Game` class contains a method `update` to update the state of the game and methods that are called by the `run` method of the `Controller` class (see below) when the corresponding command is entered, such as the `reset` method.

+ The **View** component of an MVC application manages the display (or displays). In our case, the *View* is a simple console display. Correspondingly, in our package structure, the `view` package contains the `GameView` class responsible for displaying the state of the game on the console. The constructor of `GameView` receives an instance of the `Game` class. Observe that the `view` package also contains the subclasses `ConsoleView` and `ConsoleColorsView`, the first with no colours and the second with colours; using the no-colours view requires passing the argument `NO_COLORS` to the program on start-up.

+ The **Controller**  component of an MVC application recieves the user input, parses it and passes the resulting data to the *Model* for processing before telling the *View* to display the results. In our case, the *Controller* handles only simple commands rather than, for example, capturing keyboard presses or receiving network requests. Correspondingly, in our package structure, the `control` package contains the `Controller` class whose `run` method contains the main loop of the game: while the game has not finished, call the the appropriate method of the `GameView` class to display the state of the game, then prompt the player to enter a command, then call the appropriate method of the `Game` class to process the chosen command. Notice that the `Controller` class should **not** contain any of the game logic, since the logic belongs in the *Model*.

<!-- TOC --><a name="3-stages"></a>
## 3. Implementation (in stages)

We start this section by observing that the quality of the implementation proposed here is not optimal,
one of the reasons for this being that it does not adhere to the **DRY (Don't Repeat Yourself)**
programming principle. The duplication of
code in different parts of a program makes it less maintainable, less readable and less
testable; modifying such a program is considerably more complicated and error-prone.
In the second assignment, we will *refactorise* the code, improving it by
introducing **inheritance** and **polymorphism**, two basic tools of
object-oriented programming (OOP), thereby converting it into a genuine
object-oriented program and facilitating conformity with the DRY principle.

In this section, we divide the implementation of the assignment into stages in order to guide you in your task-planning and error-correction, as well as to help your understanding; dividing the implementation into stages (thereby avoiding trying to implement everything, everywhere, all at once!) obliges you to fully understand the part of the implementation developed in each stage before you move on to the next. We also explain the function of certain important classes, methods and attributes.

<!-- TOC --><a name="31-display"></a>
### 3.1 Displaying the board

Taking into account the above instructions, we extend the skeleton code to be able to display the state of the game on the console as a few lines of text followed by the graphical representation of the board. Recall that the main loop of the game is contained in the `public void run()` method of the `Controller` class, which also requires the following attributes:

  ```java
  private Game game;
  private GameView view;
  ```

To display output on the console, we call the ``showGame()`` method of the ``GameView`` class from the `run` method. Our first task is to use this method to correctly display an empty board. Our next task is to correctly display the lines of text that must appear above the display of the board, i.e. the values of the following three attributes of the unique instance of the `Game` class that exists in the game (we will refer to this object as the `game` or the `game` object):

- `time`, which stores the remaining cycles; it is initialised to `100` and reduced by `1` on each cycle.
- `points`, which stores the player's current score; it is initialised to `0` and modified by actions of the game.
- `lives`, which stores Mario's remaining lives; it is initialised to `3` and is reduced by `1` each time Mario dies; when its value reaches `0`, the game ends and the execution terminates after displaying the message *Player loses!* (if you do not find required constants of type `String` already defined in the `Messages` class, you must add them to this class).

Next we introduce some game elements. We first create a class `Ground` which represents the ground on which other game elements can move, the simplest game element. Each game element must store its position on the board so the `Ground` class must have, amongst others, the following attribute:
  ```java
  private Position pos;
  ```
Since duplicating information is almost always bad programming practice (this is known as the DRY principle: Don't Repeat Yourself), the existence of this attribute should be taken as an indication that, internally, we will **not** be using a 2-dimensional array to store the board. Objects of this class have the property of being solid, i.e. they cannot share a position with any other object.

An instance of the `Ground` class will be displayed using the icon **▓**; the class will also need a method to return this icon:
   ```java
  public String getIcon();
  ```

Having created the `Ground` class, we must now implement the means by which objects of this class are added to the game. Such game objects will be managed by the unique instance of the `GameObjectContainer` class that exists in the game (we will refer to this object as the `container` or the `container` object), which can loosely be thought of as the internal representation of the board. This class will need an attribute containing a list of `Ground` objects (which can be implemented as a Java array of sufficient size, together with a counter) and a method to add a new `Ground` object to the game:
   ```java
  public void add(Ground ground);
  ```

Once we have managed to create objects of the `Ground` class, add them to the game and display them on the board, we can introduce more game elements in a similar way (each of them will need a `pos` attribute and a `getIcon` method):

+ The `ExitDoor` class: the unique instance of this class that exists in the game (we will refer to this object as the `exitDoor` or the `exitDoor` object) is represented on the display by the icon **🚪**. Objects of this class do not have the property of being solid. 
+ The `Goomba` class: objects of this class are represented on the display by the icon **🐻**. They do not have the property of being solid but have the property of being *mobile* (i.e. their position can change).<!-- movable (alternative spelling: moveable) means can be moved; mobile means capable of movement -->
+ The `Mario` class: the unique instance of this class that exists in the game (we will refer to this object as `mario`) is represented on the display by the icon **🧍**, when moving from left to right, and by the icon **🚶**, when moving from right to left. Objects of this class do not have the property of being solid but have the property of being *mobile*. The class contains an attribute `big` which, when activated, indicates that `mario` also occupies the cell vertically above the position stored in his `pos` attribute. This is represented on the display by drawing the same icon in both positions.

The `GameObjectContainer` class will need attributes of the appropriate type to store objects of these classes, as well as methods to add objects of these classes to the game, for which we will use the overloaded methods:
   ```java
  public void add(Goomba goomba);
  public void add(ExitDoor exit);
  public void add(Mario mario);
  ```
It is also the responsibility of the `container` to delegate the requests coming from the `game` to each of the game objects.

We can now create different worlds, or maps, each containing a different distribution of the game elements defined so far, using methods `private void initLevel0()` and `private void initLevel1()`. The map to be used can be chosen at start-up by passing the level as an argument to the application (see the image): 

![Execution options](imgs/args.png)

With this mechanism, we can add more maps if we choose. The initial state of ``initLevel1()`` should be as follows:

![Level1](imgs/mapa1.png)

and that of `initLevel0()` should show the same map but with the only goomba being the one situated at position (0,19). We recommend using this map for debugging.

This concludes the first step.


<!-- TOC --><a name="32-main-loop-basic-commands"></a>
### 3.2 Basic commands and the main loop

Now that we can display the state of the game, we turn our attention to the commands. This version of the application uses the following commands:

+ [h]elp: displays a help message. Each line of the help text comprises the name of one of the available commands, followed by a colon, followed by a brief description of what that command does. 
```java
Command > help

Available commands:
   [a]ction [[R]IGHT | [L]EFT | [U]P | [D]OWN | [S]TOP]+: user performs actions
   [u]pdate | "": user does not perform any action
   [r]eset [numLevel]: reset the game to initial configuration if not numLevel else load the numLevel map
   [h]elp: print this help message
   [e]xit: exits the game
```
+ `[e]xit`:  causes the execution to terminate after displaying the message *Player leaves game*.
+ `[r]eset [numLevel]`: reinitialises the game at the level indicated by the `numLevel` parameter, if provided, otherwise at the current level.
+ `[a]ction [[R]IGHT | [L]EFT | [U]P | [D]OWN | [S]TOP]+`: carries out the action or actions chosen by the player, then updates the game.
+ `[u]pdate | ""`: updates the game without carrying out any action chosen by the player.

Observations concerning the commands:

- The player must be able to introduce commands in upper-case, lower-case or any mixture of the two.
- The player must be able to use the specified command abbreviations (the character that appears in square brackets in the help message) and/or, in the case of the action command, the specified command-argument abbreviations.
- The empty command, i.e. simply pressing *return* or whitespace followed by *return*, should have the same effect as the `update` command.
- If the command introduced by the player does not exist (perhaps due to being badly written) or cannot be executed in the current state, the error message *Error: Unknown command: \<command-entered-by-the-player\>* must be displayed.
- After the execution of a command that doesn't change the state of the game, e.g. `help`, or after an error, the board must not be displayed.

We suggest that you first implement the `exit` command, then the `help` command, then the `reset` command. The `action` command is explained below. Note that the `reset` command must reinitialise the value of the `time` attribute but **not** that of the `points` and `lives` attributes.

This concludes the second step.

<!-- TOC --><a name="33-automatic-movement-and-update"></a>
### 3.3 Automatic movement and updating the state of the game

We now enable some of the game elements to move and implement the updating of the state of the game. Updating may occur due to use of the `[u]pdate|""` command by the player or as part of other commands (currently, only the `action` command). The key to the implementation of the updating is the definition of the method `public void update()` in each of the classes of the objects that are updatable (which, in the current game is synonymous with *mobile*), currently only `Mario` and `Goomba`.
- The `update` method of the `Goomba` class only needs to consider automatic movement.
- The `update` method of the `Mario` class must take into account:
  1. automatic movement,
  2. any actions introduced by the player (in the case where the update occurs as part of the `action` command),
  3. any collisions between Mario and other objects.

#### The `update()` method of the `Goomba` class

The behaviour of a `Goomba` object is completely automatic, i.e. cannot be changed by commands.
- If it has a solid object underneath, it advances (i.e. moves horizontally in its currently-set direction, its initial movement being from right to left) one cell per cycle.
- If it collides with a solid object or with the side-wall of the board, it starts to move in the opposite direction.
- If it does not have a solid object underneath, it falls (moves vertically down one cell per cycle) and continues falling each cycle until either it encounters a solid object underneath it, or it leaves the board via the bottom edge; in the latter case, it dies and must be removed from the game (i.e. deleted from the list of `Goomba` objects).

#### The `update()` method of the `Mario` class`

The automatic movement of `mario` is similar, but not identical, to that of the `Goomba` objects: 
- Initially, `mario`'s direction of movement is set as left-to-right. 
- When `mario` dies, he loses a life and notifies the game of this loss, in order for it to make the necessary adjustments, namely
    - If the number of lives is still non-zero, reset the game
    - If the number of lives is now zero, terminate the game after printing the message *Game over, player loses!*. 
    
As well as this automatic movement, the `update` method of the `Mario` class must also take into account:
- The actions added by the player (stored in an object of the `ActionList` class, see below). 
- The collisions with other objects on the board (namely, `Goomba` objects or the `exitDoor`).

This will be dealt with in the following section.

#### The `update()` method of `Game` and `GameObjectContainer`

The updating is coordinated by the `game` object using the `public void update()` method of the `Game` class. This method:
1. Updates the time
2. Calls the `update()` method of the associated `container` object. 
3. which, calls the `update()` methods of each of the associated updatable game objects.

In order for the output (display) of our program to be the same as that assumed by the tests, it is **very important** that you respect the order in which the associated mobile game objects are to be updated.
1. **First Mario**. 
2. **Second Goombas**. 

This concludes the third step.

<!-- TOC --><a name="34-mario-actions"></a>
### 3.4 Mario actions

As well as automatic movement, `mario` must carry out the actions specified by the player as arguments of the `action` command. Since the `action` command admits multiple arguments, an `ActionList` class is used to manage the actions corresponding to these arguments. Recall that the player must be able to introduce the action-command arguments in upper-case, lower-case or any mixture of the two, and must also be able to use their abbreviations. For example:
```java
Command > action up UP rIGhT
```
or
```java
Command > a u u r
```

#### Permissible actions

The permissible actions specify `mario` movements and are represented by the literals of the `Action` enum.

- **LEFT / RIGHT**: `mario` changes his direction of horizontal movement to that indicated and advances one step in that direction; the icon used to display `mario` changes accordingly.
- **UP**: causes `mario` to move vertically upwards one cell, without changing his direction of horizontal movement or his icon.
- **DOWN**: If Mario is in the air (i.e. not on the ground), he falls until either he reaches a solid object (currently only the ground) or he leaves the board. <!-- does he die? -->If he is on the ground, his horizontal movement is deactivated (so this is equivalent to using the `STOP` action) and his icon changes to **🧑**, -- doubled, if `mario` is currently big -- to indicate this.
- **STOP**: deactivates `mario`'s horizontal movement, changing his icon to **🧑** -- doubled, if `mario` is currently big. In this deactivated state, vertical actions (`UP` or `DOWN`) execute normally, with the icon remaining **🧑** ; note that, in this state, a `DOWN` action with `mario` on the ground has no effect.


#### The `ActionList` class

The `ActionList` class manages the actions that the player introduces via the `action` command for `mario` to carry out in the current cycle:
- It stores a FIFO list of actions to be executed on the next `mario` update.
- It applies the following restrictions in order to avoid incoherent combinations of actions, where (`LEFT`,`RIGHT`) and (`UP`, `DOWN`) are opposite pairs of actions:
  - opposite actions cannot be executed in the same cycle; if the arguments of an `action` command contains opposites, the first is used and subsequent opposite actions are ignored.
  - up to 4 occurrences of the same action can be executed on the same cycle; if the arguments of an `action` command contain more than 4 occurrences of the same action, subsequent repetitions are ignored.


#### Action execution

After the player introduces an `action` command, execution proceeds as follows:

1. The unique instance of the `Controller` class that exists in the game (we will refer to this object as the `controller` or the controller object) *parses* each command argument (in fact, it should delegate this task to a method of the `Action` class) to create an instance of the `Action` class, i.e. an enum literal, then calls an `addAction(Action act)` method of the `game` in order to add this `Action` object to the action list managed by `mario`.
2. On the each cycle, as part of the update, `mario` executes all the pending actions (those on the action list) in order of arrival (i.e. a FIFO list), while respecting the above restrictions on combining actions. After executing all the pending actions, the action list is empty (alternatively, we could create a new list on each use of the `action` command but this could be considered to be an abuse of the garbage collection service).
3. If `mario`'s position has not changed after executing all the actions on the action list, the automatic movement is applied, otherwise, it is **not** applied.

This concludes the fourth step.

<!-- TOC --><a name="35-collisions-mario-exitdoor"></a>
### 3.5 Collisions mario  ↔  exitDoor

We now turn to the question of collisions between objects, starting with the collision between `mario` and the `exitDoor`. This collision is to be managed using the following method of the `Mario` class.

```java
public boolean interactWith(ExitDoor other);
```

The `interactWith` method checks if `mario` is on the same position as the `exitDoor` and, if so, invokes the following method of the `Game` class:

```java
public void marioExited()
```

The `marioExited` method adds the value returned by the `remainingTime` method multiplied by 10 to the player's score, and terminates the program after printing the following message on the console: *Thanks, Mario! Your mission is complete.* Checking for the interaction between `mario` and the exit door must be carried out on each cycle as part of the update: the `update` method of the `container` traverses all the instances of the `ExitDoor` class (currently, there is only one) to see if any of these objects has a collision with `mario`·

The structure of the update code now looks as follows:
```text
call update method on the game object
└─> decrement time
└─> call update method on the container object
      ├─> call update method on mario ──> execute actions and/or automatic movement
      ├─> checkMarioinExit ──> check for collisions between mario and the exitDoor
      └─> for g in Goombas:
             call update method on g ──> automatic movement
```

This concludes the fifth step.

<!-- TOC --><a name="36-collisions-mario-goombas"></a>
### 3.6 Collisions mario ↔ goombas

We first need to define the rules of the game governing mario-goomba collisions. They are as follows:
+ The outcome for the goomba is that it dies and, in consequence, `100` points is added to the player's score.
+ The outcome for `mario` depends on his state:
  - If he is falling and has fallen onto a goomba, his state remains unchanged.
  - If he is not falling
    - if he is currently `big`, he becomes not `big`
    - if he is currently not `big`, he loses a life.

We now describe how these rules are to be implemented. Note that checking for collisions must be carried out after every single movement `mario` makes, whether or not this movement is automatic. As we will see, the mechanism used to implement collisions is not symmetric w.r.t. the two parties involved. To implement the rules, we use two methods, the first being a method of the `Mario` class:

```java
public boolean interactWith(Goomba other)
```
which checks whether or not `mario` has collided with the `Goomba` object passed via the `other` parameter. If so, it implements the effect of the collision on the owning object of the `Mario` class and notifies the `Goomba` object that it has collided with `mario` by calling the following method of the `Goomba` class on that object:

```java
public boolean receiveInteraction(Mario other)
```
This method is responsible for implementing the effect of the collision on the goomba and for any necessary modifications to the game, in particular, concerning the player's score.

But who calls `mario`'s `interactWith` method? To answer this question, note that it is the responsability of the `container` to manage the game objects that it contains and therefore, it is the `container` via the method:

```java
public void doInteractionsFrom(Mario mario)
```
which calls `mario`'s `interactWith` method, once for each goomba in the game.

In turn, the `doInteractionsFrom` method of the `container` is called by a method with the same method signature (method signature = method name + number and type of parameters + return type) in the `game`, i.e. the `doInteractionsFrom` method of the `Game` class simply delegates to the `doInteractionsFrom` method of the `Container` class. 

We have described the mechanism used for the `game` to check for `mario`-goomba collisions. Now we need to specify when this mechanism must be used. To detect all possible `mario`-goomba collisions, it must be used after `mario` movements as well as after goomba movements:
- for each `mario` movement, whether this is due to executing an action or is the automatic movement; in this case, the call to the `doInteractionsFrom` method of the `Game` class is made from the `Mario` class,
- after all the goombas have moved; in this case the call to the `doInteractionsFrom` method of the `Game` class can be made from the `GameObjectContainer` class.

Finally, the `update` method of the container should remove any dead goombas from the list of `Goomba` objects that it maintains.

The structure of the `update` code now looks as follows:
```text
call update method on the game object
└─> decrement time
└─> call update method on the container object
      ├─> call update method on mario ──> execute actions and/or automatic movement
      │       └─> call doInteractionsFrom method on the game object
      │              └─> call doInteractionsFrom method on the container object
      │                     └─> for g in Goombas:
      │                            call interactWith(g) on mario
      ├─> checkMarioinExit ──> check for collisions between mario and the exitDoor
      ├─> for g in Goombas:
      │       call update() on g ──> automatic movement
      └─> call doInteractionsFrom method on the game object
      │       └─> call doInteractionsFrom method on the container object
      │              └─> for g in Goombas:
      │                     call interactWith(g) on mario
      └─> cleanUp ──> eliminate any dead goombas
```

This concludes the last step.

---

<!-- TOC --><a name="4-submission"></a>
## 4. Submission

The assignment must be submitted as a single compressed (with `zip`) archive via the
Campus Virtual submission mechanism not later than the date and time indicated at the start of this
document [^1]. The zip archive should contain at least the following [^2].

- A directory called `src` containing the Java source code of your solution,
- a file called `students.txt`, containing the names of the members of your group.

**Do not include the files generated on compilation**, i.e. the `.class` files,
which, if you created your project following the procedure indicated in Assignment 0, should
be in a separate directory called `bin`.

It is good practice to also include:

- a directory called `doc` containing the API documentation in HTML format generated
  automatically from the Java source code of your solution using the *javadoc* tool.

However, it is not obligatory and the contents of any `doc` directory you may include will **not**
contribute to your grade for the assignment. Note that using *javadoc* involves adding comments to your
code in the *javadoc* format otherwise the HTML that you generate will contain almost no information.

[^1]: To generate the `zip` file, you may find it helpful to use the Eclipse option *File > Export*.
[^2]: You may also include the project information files generated by Eclipse.


<!-- TOC --><a name="5-testing"></a>
## 5. Testing

Together with the instructions for the assignment, you have a directory of program traces.
In the directory there are files following one of two nomenclatures:

- `00_1-play_input.txt`: the test case with input `1` on map `00` whose purpose is to test the `play` command.
- `00_1-play_expected.txt`: the expected output for the previous test case
- `01_1-command_input.txt`: the test case with input `1` on map `01` whose purpose is to test the commands.
- `01_1-command_expected.txt`: the expected output for the previous test case

To take the input of a Java program from a given input text file and send the output to an output text file in Eclipse, you can configure the *redirection* of the standard input and standard output / standard error
in the `Common` tab of the `Run Configurations` window as shown in the following figure. The easiest
thing to do is to create at least one `Run Configuration` for each test case.

![Input-output redirection](imgs/runConfigurationsCommon.png)

There are many free programs to visually compare files and thereby check that the output of your
program coincides with the expected output for each of the test cases we provide. In particular,
there is one integrated in Eclipse: select the files you wish to compare, press the right button
and then select `Compare With > Each other` in the pop-up menu, as shown in the following figure:

![Comparing files in Eclipse](imgs/Eclipse_Compare_EachOther.png)

Another window will appear showing clearly the differences between the two text files. Two other possibilities are [Beyond compare](https://www.scootersoftware.com/) and [DiffMerge](https://sourcegear.com/diffmerge/).

If you detect an error in the output of any of the test cases provided please let the lecturer know ASAP so that we can correct it.

You should also create and execute your own test cases to check the correctness of other executions of your implementation, as we will do during the correction of the assignments.

<!-- TOC --><a name="51-JUnit"></a>
### Automating the tests: JUnit

In order to simplify the tests and automate the above process, we will "abuse" the JUnit (https://junit.org/) library included in Eclipse. JUnit is a framework for executing test cases on Java code that you will most likely come across in other courses of your degree. If you have not already done so, you will need to add the JUnit library to your Java project. To do so, select the *Properties* option of the *Project* menu then select *Java Build Path* and then the *Libraries* tab. With *Classpath* selected (not *ModulePath*) click on the *Add Library...* button.

![](./imgs/jUnit/00-ProjectProjerties.jpg)

In the window that pops up select *JUnit* then click on the *Finish* button.

![](./imgs/jUnit/01-AddJUnit.jpg)

On returning to the project-properties window, click on the *Apply and Close* button.

A file containing the class `tp1.Tests` is included in the skeleton code we provided. It contains one JUnit test cases for each of the test cases defined by the input-output as stated above. If the build path has been properly configured, on clicking with the right button on this file and then selecting *Run As*, the option *JUnit Test* should appear.

![](./imgs/jUnit/02-RunAsJUnitTest.jpg)

If you execute the tests, Eclipse shows a view in which we can see the result of the tests and can execute those that have failed, individually or all at once. We only use JUnit to compare the output of your program with the expected output. If your output does not conform to what is expected, you will need to use the comparison procedure explained above to see the details.

<!-- ![Fallo JUnit](./imgs/jUnit/03-JUnitFailed.jpg) -->
<figure>
    <img src="./imgs/jUnit/03-JUnitFailed.jpg"
         alt="Fallo JUnit">
    <figcaption>Fallan las pruebas JUnit</figcaption>
</figure>

<!-- ![Todas las pruebas JUnit tienen éxito](./imgs/jUnit/04-JUnitPass.jpg) -->
<figure>
    <img src="./imgs/jUnit/04-JUnitPass.jpg"
         alt="Éxito JUnit">
    <figcaption>Todas las pruebas JUnit tienen éxito</figcaption>
</figure>


