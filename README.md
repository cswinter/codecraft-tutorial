# CodeCraft Tutorial

This is a tutorial which describes how to implement an AI for the programming game CodeCraft.
You "play" CodeCraft by writing a program in Scala or Java and I am going to assume that you are already familiar with one of these languages.
If you encounter a bug, or have a question, or want to give feedback you can post to this [Google group](https://groups.google.com/forum/#!forum/codecraftgame) or send me an email at codecraftgame@gmail.com.

## Basics

To get things set up, you need to create a new project in your favourite IDE (or whatever else you use) which imports [codecraft.jar](INSERT HYPERLINK HERE).
Alternatively, this repo also contains bare bones templates for Scala and Java in the folders scala-template and java-template.
They can be run with from the command line with `sbt run` and `javac *.java -cp codecraft-0.1.0.jar && java -cp '.;codecraft-0.1.0.jar' Main` respectively.

The main entry point into the game is the TheGameMaster object.
You can start the first level by calling TheGameMaster.runLevel1 as such:

```scala
import cwinter.codecraft.core.api._
    
object Main {
  def main(args: Array[String]): Unit = {
    TheGameMaster.runLevel1(new Mothership)
  }
}
```

The same in Java:

```java
import cwinter.codecraft.core.api.TheGameMaster;

class Main {
  public static void main(String[] args) {
    TheGameMaster.runLevel1(new Mothership());
  }
}
```


The `runLevel1` method expects an object of type `DroneController` as argument.
This object will implement the AI for your initial unit.
So let's create a new file to implement this class:

```scala
import cwinter.codecraft.core.api._
    
class Mothership extends DroneController {
}
```
For Java, use the `JDroneController` variant instead. It is essentially identical to `DroneController`, but its getters return Java collections rather than their Scala counterparts:
```java
import cwinter.codecraft.core.api.*;

class Mothership extends JDroneController {
}
```

As you can see, the Mothership class doesn't contain any code so your unit AI won't do anything yet.
However, you should now be able to run the program.
If everything works correctly, you will see a window that displays the game world.
The camera is initially centered on your mothership, which looks like a septagon with various shapes inside (its modules, more on that later).
The green pentagons are mineral crystals, which can be harvested to gain more resources and construct new drones.
You can move your camera using the WASD keys and zoom in/out with Q and E.
Somewhere to the left of your mothership you will find your orange opponent, who is already busy plotting your downfall.

## Building a scouting drone

Ok, so let's actually tell our mothership to do something.
The first thing we want is to build a new drone that will scout the map, harvest minerals and bring them back to the mothership.
To do so, we override the `DroneController.onSpawn` method inside the `Mothership` class.
This method is called automatically by the game engine when our drone first spawns.
Inside `onSpawn`, we call `buildDrone(droneSpec, controller)`. This method take two arguments:

* `droneSpec` specifies what modules our new drone will have. (in this case, we want two storage modules which will allow us to harvest mineral crystals)
* `controller` is another DroneController that will govern the behaviour of our new Drone.

Add the following code to your Motership class:

```scala
override def onSpawn(): Unit = {
  val harvesterSpec = new DroneSpec(storageModules = 2)
  buildDrone(harvesterSpec, new HarvesterController)
}
```
```java
@Override public void onSpawn() {
  DroneSpec harvesterSpec = new DroneSpec(2, 0, 0, 0, 0, 0);
  buildDrone(harvesterSpec, new HarvesterController());
}
```

Of course we still need to implement the `HarvesterController`, so create a new file with the following contents:

```scala
import cwinter.codecraft.core.api._
import scala.util.Random
    
class HarvesterController extends DroneController {
  override def onTick(): Unit = {
    if (Random.nextInt(30) == 0) {
      val newDirection = 2 * math.Pi * Random.nextDouble()
      moveInDirection(newDirection)
    }
  }
}
```
```java
import cwinter.codecraft.core.api.*;
import java.util.Random;

class HarvesterController extends JDroneController {
  static Random rng = new Random();

  @Override public void onTick() {
    if (rng.nextInt(30) == 0) {
      Double newDirection = 2 * Math.PI * rng.nextDouble();
      moveInDirection(newDirection);
    }
  }
}
```
    
This time, we override the `onTick` method which is called on every timestep.
On random timesteps, with a chance of 1 in 30, we give the drone a command to move into a new random direction using the `moveInDirection` method.
You should now run the program again and verify that, in fact, your mothership will construct a new drone which moves randomly across the map.

## Harvesting resources

We still want to harvest resources and return them to the mothership.
For this, we override the `onMineralEntersVision` method which is called whenever a mineral crystal enters the sight radius of our drone.
When this happens, we want to stop scouting and move towards the mineral crystal.
Once we have arrived, the `onArrivesAtMineral` method is called, where we give orders to harvest the mineral and return to the mothership.
Once this happens, the `onArrivesAtDrone` method will be called where we give orders to deposit the mineral crystal and go back into scouting mode.
The HarvesterController class should now look like this:

```scala
class HarvesterController(val mothership: DroneController) extends DroneController {
  private var isScouting = true

  override def onTick(): Unit = {
    if (isScouting && Random.nextInt(30) == 0) {
      val newDirection = 2 * math.PI * Random.nextDouble()
      moveInDirection(newDirection)
    }
  }

  // when we see a mineral crystal, stop scouting and move towards it
  override def onMineralEntersVision(mineralCrystal: MineralCrystalHandle): Unit = {
    moveTo(mineralCrystal)
    isScouting = false
  }

  // once we arrive at the mineral, harvest it and bring it back to the mothership
  override def onArrivesAtMineral(mineral: MineralCrystalHandle): Unit = {
    harvest(mineral)
    moveTo(mothership)
  }

  // once we arrive at the mothership, deposit the crystal and start scouting again
  override def onArrivesAtDrone(drone: DroneHandle): Unit = {
    giveMineralsTo(mothership)
    isScouting = true
  }
}
```
```java
class HarvesterController extends JDroneController {
  static Random rng = new Random();
  private Boolean isScouting = true;
  private JDroneController mothership;


  public HarvesterController(JDroneController mothership) {
    this.mothership = mothership;
  }


  @Override public void onTick() {
    if (isScouting && rng.nextInt(30) == 0) {
      Double newDirection = 2 * Math.PI * rng.nextDouble();
      moveInDirection(newDirection);
    }
  }

  // whe we see a mineral crystal, stop scouting and move towards it
  @Override public void onMineralEntersVision(MineralCrystalHandle mineral) {
    if (isScouting) {
      moveTo(mineral);
      isScouting = false;
    }
  }

  // once we arrive at the mineral, harvest it and bring it back to the mothership
  @Override public void onArrivesAtMineral(MineralCrystalHandle mineral) {
    harvest(mineral);
    moveTo(mothership);
  }

  // once we arrive at the mothership, deposit the crystal and switch back to scouting mode
  @Override public void onArrivesAtDrone(DroneHandle drone) {
    giveMineralsTo(mothership);
    isScouting = true;
  }
}
```

We also need to make a small change to the `Mothership` controller.
The `HarvesterController` now expects a reference to the mothership as argument, so it knows where to return the minerals.
We are also going to move the construction code into the `onTick` method so that we keep building new drones.
The `Mothership` class now looks like this:

```scala
class Mothership extends DroneController {
  final val HarvesterSpec = new DroneSpec(storageModules = 2)
    
  override def onTick(): Unit = {
    if (!isConstructing) {
      buildDrone(HarvesterSpec, new Harvester(this))
    }
  }
}
```
```java
import cwinter.codecraft.core.api.*;

class Mothership extends JDroneController {
  static final DroneSpec HARVESTER_SPEC = new DroneSpec(2, 0, 0, 0, 0, 0);

  @Override public void onTick() {
    if (!isConstructing()) {
      buildDrone(HARVESTER_SPEC, new HarvesterController(this));
    }
  }
}
```

If you run your program again now, you should see a growing armada of harvesters collecting all the mineral crystals.

## Combat

Now that you have laid the economic foundations for your drone empire, it is time to start thinking about how to beat your opponent.
By now you probably got the hang of how CodeCraft works, so I will just give you a high level overview of how to implement your army.
After you have built some harvesters, you will want to start production on a different type of drone.
The HarvesterSpec won't do for this, you will need to define another spec without `storageModules` which instead has one or more `missileBatteries`, and maybe even `shieldGenerators` (if you are using Java, those correspond to the 2nd and 6th argument of the DroneSpec constructor respectively).
Another controller will be required as well, and you might find the following methods useful:

* The `DroneController` method `dronesInSight` returns a `Set` of all `DroneHandle`s which can be seen by this drone controller
* The `DroneHandle` class has a method `isEnemy` which tells you whether that drone is an enemy
* The `DroneController` method `isInMissileRange(target: DroneHandle)` can be used to check whether some drone is within the range of your missiles
* The `DroneController` method `shootMissiles(target: DroneHandle)` will fire all your missiles at the drone `target`
* If you are using Java: method parentheses aren't optional in Java, so you will need to write e.g. `dronesInSight()` and `isEnemy()`

If you don't quite manage to get all of this to work, you can check out the scala-solution and java-solution directories in this repo, which contain a full implementation of everything described in this tutorial.
If you want to go even further, check out the next section which gives an overview of all the other parts of the API which haven't been covered yet.
If you manage beat all the levels and want your AI included in the next release, send me a link to your code at codecraft@gmail.com.


## API overview

I plan to have comprehensive documentation of the API eventually, for now I will just give a general description of how everything is organized and list the names of all of the methods of interest. It shouldn't be too hard to figure out what most of them do.

### `TheGameMaster`
To configure and start the game, you use `TheGameMaster` object.
`TheGameMaster` has methods `runLevel1`, `runLevel2`, and `runLevel3` which take as an argument the `DroneController` for your mothership and start the corresponding level.
There is also a `runGame` method which allows you to start a game with two custom `DroneController`s.
The game automatically records replays of all games in the folder `~/.codecraft/replays`.
You can run the last recorded replay using `runLastReplay` and run the replay with a specific filename using `runReplay`.

### `DroneController`
Almost all interactions with the game world will go through this class.
If you are using Java, you should use the `JDroneController` class instead, which is almost identical but returns Java collections rather than their Scala counterparts.
`DroneController` has the following methods which you can override to be informed of various events:

    onSpawn()
    onDeath()
    onTick()
    onMineralEntersVision(mineralCrystal: MineralCrystalHandle)
    onDroneEntersVision(drone: DroneHandle)
    onArrivesAtPosition()
    onArrivesAtMineral(mineralCrystal: MineralCrystalHandle)
    onArrivesAtDrone(drone: DroneHandle)

On every timestep, `onTick` will be called after all the other methods. Those may be called in any order.

The following methods issue commands to your drone:

    moveInDirection(directionVector: Vector2)
    moveInDirection(direction: Double)
    moveTo(otherDrone: DroneHandle)
    moveTo(mineralCrystal: MineralCrystalHandle)
    moveTo(position: Vector2)
    harvest(mineralCrystal: MineralCrystalHandle)
    depositMinerals(otherDrone: DroneHandle)
    buildDrone(spec: DroneSpec, controller: DroneController)
    processMineral(mineralCrystal: MineralCrystalHandle)
    shootMissiles(target: DroneHandle)

The `DroneController` class and also the `DroneHandle` class, which may be an enemy drone, expose various properties:

    position: Vector2
    weaponsCooldown: Int
    isVisible: Boolean
    spec: DroneSpec
    player: Player
    hitpoints: Int
    isEnemy: Boolean

The following properties are specific to `DroneController`

    isInMissileRange(droneHandle: DroneHandle): Boolean
	  isConstructing: Boolean
    availableStorage: Int
    availableFactories: Int
    storedMinerals: Seq[MineralCrystalHandle]
    dronesInSight: Set[DroneHandle]
    worldSize: Rectangle
    orientation: Double

### `DroneSpec`

Used to specify how many of each type of module a drone has.
`DroneSpec` has the following parameters, and also class members of the same name:

* `storageModules` can be used to store minerals. More modules allow for storing more/larger minerals.
* `missileBatteries` allow the drone to shoot homing missiles. More modules increase the numbers of missiles shot.
* `refineries` allow the drone to convert minerals into resources. More modules allows for processing more/larger minerals.
* `manipulators` allow the drone to construct new drones. More modules increase construction speed.
* `engines` increase movement speed. The movement speed is determined by the *relative* number of engines. (so other modules effectively decrease movement speed)
* `shieldGenerators` give the drone an additional 7 hitpoints each. Shields regenerate over time.

Currently, the total number of modules is limited to 10, but this restriction will likely be lifted in the future.

### `MineralCrystalHandle`

Represents a mineral crystal.
Each mineral crystal has a `size`, and to harvest a mineral you need at least `size` free storage modules.
The class also has a `position` and a `harvested` method that will tell you whether the mineral crystal has already been harvested.

### `Vector2`

CodeCraft uses the immutable Vector2 class for everything that is represented by a 2D vector, such as the position of the Drones. (the world can always use another broken vector implementation)

There are two ways to create vectors: `Vector2(x, y)` will create a vector with components x and y. `Vector2(a)` will create a unit vector rotated by an angle of a radians. Addition, subtraction, multiplication and division work as expected:

    > 2 * Vector2(0.5, 0) + Vector2(0, 10) / 10 - Vector2(10, 0)
    res0: Vector2 = Vector2(-9, 1)

If you are using Java, you can create vectors with `new Vector2(x, y)` and you can use the methods `plus`, `times` and `minus` in place of the symbolic variants.

Additionally, you may find the following methods on `Vector2` useful:

    dot(rhs: Vector2): Double
    length: Double
    orientation: Double
    lengthSquared: Double
    normalized: Vector2
    rotated(angle: Double): Vector2

### `Debug`

You can call `Debug.drawText(text: String, xPos: Double, yPos: Double, color: ColorRGBA)` to place a string anywhere in the game world. This is only valid for one timestep, so you if you will need to call this method on every timestep on which you want the text to be displayed.

E.g. if you wanted your drone's to display their position, you could use this code:

    override def onTick(): Unit = {
      Debug.drawText(position.toString, position.x, position.y, ColorRGBA(1, 1, 1, 1))
    }
