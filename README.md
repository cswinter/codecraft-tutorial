# CodeCraft Tutorial [![Join the chat at https://gitter.im/cswinter/CodeCraftGame](https://badges.gitter.im/cswinter/CodeCraftGame.svg)](https://gitter.im/cswinter/CodeCraftGame?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.codecraftgame/codecraft_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.codecraftgame/codecraft_2.11)

This is a tutorial which describes how to implement an AI for the programming game CodeCraft.
You "play" CodeCraft by writing a program in Scala or Java and I am going to assume that you are already familiar with one of these languages.
If you encounter a bug, have a question, or want to give feedback you can go on [Gitter](https://gitter.im/cswinter/CodeCraftGame) or send me an email at codecraftgame@gmail.com.

## Basics

To get things set up, you need to create a new project and add the [CodeCraft library](http://search.maven.org/#artifactdetails%7Corg.codecraftgame%7Ccodecraft_2.11%7C0.6.0%7C) as a dependency (in sbt: `libraryDependencies += "org.codecraftgame" % "codecraft_2.11" % "0.6.0"`).
An implementation of the setup described in this section can be found in the folders scala-template and java-template.

The main entry point into the game is the `TheGameMaster` object.
You can start the first level by calling `TheGameMaster.runLevel1` as such:

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
The green pentagons are mineral crystals, which can be harvested to gain resources and construct additional drones.
You can move your camera using the WASD keys and zoom in/out with Q and E.
Somewhere to the left of your mothership you will find your orange opponent, who is already busy plotting your downfall.

## Building a scouting drone

Ok, so let's actually tell our mothership to do something.
The first thing we want is to build a new drone that will scout the map, harvest minerals and bring them back to the mothership.
To do so, we override the `DroneController.onSpawn` method inside the `Mothership` class.
This method is called automatically by the game engine when our drone first spawns.
Inside `onSpawn`, we call `buildDrone(controller, droneSpec)`. This method take two arguments:

* `controller` is another DroneController that will govern the behaviour of our new Drone.
* `droneSpec` is a DroneSpec object which specifies what modules our new drone will have. (in this case, we want two storage modules which will allow us to harvest and transport resources)

In Scala, you can use a simpler variant of buildDrone that allows you to directly specify with modules using named parameters instead of creating a DroneSpec object.
Add the following code to your Mothership class:

```scala
override def onSpawn(): Unit = buildDrone(new Harvester, storageModules = 2)
```
```java
@Override public void onSpawn() {
  DroneSpec harvesterSpec = new DroneSpec().withStorageModules(2);
  buildDrone(new Harvester(), harvesterSpec);
}
```

Of course we still need to implement the `Harvester`, so create a new file with the following contents:

```scala
import cwinter.codecraft.core.api._
import cwinter.codecraft.util.maths.Vector2
import scala.util.Random
    
class Harvester extends DroneController {
  override def onTick(): Unit = {
    if (!isMoving) {
      val randomDirection = Vector2(2 * math.Pi * Random.nextDouble())
      val targetPosition = position + 500 * randomDirection
      moveTo(targetPosition)
    }
  }
}
```
```java
import cwinter.codecraft.core.api.*;
import cwinter.codecraft.util.maths.Vector2;
import java.util.Random;

class Harvester extends JDroneController {
  static Random rng = new Random();

  @Override public void onTick() {
    if (!isMoving()) {
      Vector2 randomDirection = new Vector2(2 * Math.PI * rng.nextDouble());
      Vector2 targetPosition = position.plus(randomDirection.times(500));
      moveTo(targetPosition);
    }
  }
}
```

This time, we override the `onTick` method which is called on every timestep.
First, we test whether the drone is currently moving using the isMoving property.
If this is not the case, we give the drone a command to move into a new random direction for 100 timesteps using the `moveInDirection` method.
You should now run the program again and verify that your mothership constructs a new drone which moves randomly across the map.

## Harvesting resources

We still want to harvest resources and return them to the mothership.
For this, we override the `onMineralEntersVision` method which is called whenever a mineral crystal enters the sight radius of our drone.
When this happens, we want to stop scouting and move towards the mineral crystal.
Once we have arrived, the `onArrivesAtMineral` method is called, where we give orders to harvest the mineral.
We also modify our code in the `onTick` method to send the drone back to the mothership when it's storage is full.
Once the drone arrives there, the `onArrivesAtDrone` method will be called where we give orders to deposit the mineral crystals.
The HarvesterController class should now look like this:

```scala
class Harvester(mothership: DroneController) extends DroneController {
  override def onTick(): Unit = {
    if (!isMoving && !isHarvesting) {
      if (availableStorage == 0) moveTo(mothership)
      else {
        val randomDirection = Vector2(2 * math.Pi * Random.nextDouble())
        val targetPosition = position + 500 * randomDirection
        moveTo(targetPosition)
      }
    }
  }

  override def onMineralEntersVision(mineral: MineralCrystal) =
    if (availableStorage > 0) moveTo(mineral)

  override def onArrivesAtMineral(mineral: MineralCrystal) = harvest(mineral)

  override def onArrivesAtDrone(drone: Drone) = giveResourcesTo(drone)
}
```
```java
class Harvester extends JDroneController {
  static Random rng = new Random();
  private JDroneController mothership;


  public Harvester(JDroneController mothership) {
    this.mothership = mothership;
  }


  @Override public void onTick() {
    if (!isMoving() && !isHarvesting()) {
      if (availableStorage() == 0) moveTo(mothership);
      else {
        Vector2 randomDirection = Vector2(2 * Math.PI * rng.nextDouble());
        Vector2 targetPosition = position.plus(randomDirection.times(500));
        moveTo(targetPosition);
      }
    }
  }

  @Override public void onMineralEntersVision(MineralCrystal mineral) {
    if (availableStorage() > 0) moveTo(mineral);
  }

  @Override public void onArrivesAtMineral(MineralCrystal mineral) {
    harvest(mineral);
  }

  @Override public void onArrivesAtDrone(Drone drone) {
    giveResourcesTo(drone);
  }
}
```

We also need to make a small change to the `Mothership` controller.
The `HarvesterController` now expects a reference to the mothership as argument, so it knows where to return the minerals.
We are also going to move the construction code into the `onTick` method so that we keep building new drones.
The `Mothership` class now looks like this:

```scala
class Mothership extends DroneController {
  override def onTick(): Unit =
    if (!isConstructing) buildDrone(new Harvester(this), storageModules = 2)
}
```
```java
import cwinter.codecraft.core.api.*;

class Mothership extends JDroneController {
  static final DroneSpec HARVESTER_SPEC = new DroneSpec().withStorageModules(2);

  @Override public void onTick() {
    if (!isConstructing()) {
      buildDrone(new Harvester(this), HARVESTER_SPEC);
    }
  }
}
```

If you run your program again now, you should see a growing armada of harvesters collecting all the mineral crystals.

## Combat

Now that you have laid the economic foundations for your drone empire, it is time to start thinking about how to beat your opponent.
By now you probably got the hang of how CodeCraft works, so I will just give you a high level overview of how to implement your army.
After you have built some harvesters, you will want to start production on a different type of drone.
Instead of `storageModules`, it should be equipped with one or more `missileBatteries` and maybe even `shieldGenerators`.
Another controller will be required as well, and you will find the following methods useful:

* The `DroneController` method `dronesInSight` returns a `Set` of all `Drone`s which can be seen by this drone controller
* The `Drone` class has a method `isEnemy` which tells you whether that drone is an enemy
* The `DroneController` method `isInMissileRange(target: Drone)` can be used to check whether some drone is within the range of your missiles
* The `DroneController` method `fireMissilesAt(target: Drone)` will fire all your missiles at the drone `target`
* If you are using Java method parentheses aren't optional, so you will need to write e.g. `dronesInSight()` and `isEnemy()`

If you don't quite manage to get all of this to work, you can check out the scala-solution and java-solution directories in this repo, which contain a full implementation of everything described in this tutorial.
If you want to go even further, check out the next section which gives an overview of all the other parts of the API which haven't been covered yet.
If you write an AI that uses a different strategy than those in the current levels (or which beats all levels), please send me a link to your code at codecraft@gmail.com and I'll include in the next release.


## What to do next

Hopefully this tutorial has succeded in giving you a good understanding of what CodeCraft is about and how to use it.
If you want to continue building out your AI, you can get answers to any questions you have from the [comprehensive documentation](http://www.codecraftgame.org/docs/api/index.html) or [fellow CodeCraft users](http://www.codecraftgame.org/community).
In addtiion to that, this section gives a quick overview of the most useful parts of the API.

### `TheGameMaster`
To configure and start the game, you use `TheGameMaster` object.
`TheGameMaster` has methods `runLevel1`, `runLevel2`, ..., `runLevel7` which take as an argument the `DroneController` for your mothership and start the corresponding level.
There is also a `runGame` method which allows you to start a game with two custom `DroneController`s.
The game automatically records replays of all games in the folder `~/.codecraft/replays`.
You can run the last recorded replay using `runLastReplay` and run the replay with a specific filename using `runReplay`.

### `DroneController`
Almost all interactions with the game world goes through this class.
If you are using Java, you should use the `JDroneController` class instead, which is almost identical but returns Java collections rather than their Scala counterparts.
`DroneController` has three different kinds of methods:

* Event handlers such as `onSpawn` and `onDroneEntersVision`. These are automatically called by the game on specific events and you can override them to respond to these events.
* Commands such as `moveTo` and `buildDrone`. You can call these methods to make your drones perform various actions.
* Properties such as `position` and `hitpoints` which allow you to query the current state of the drone. 

You can find a complete description of all of them in the [API reference][API#DroneController].
In some methods (e.g. `onDroneEntersVision`) you are given a [`Drone`][API#Drone] object.
Since this could reference an enemy drone, it only exposes a subset of the properties and none of the event and command methods.

### `Vector2`
`Vector2` is an immutable 2D vector and used throughout CodeCraft.
It defines various methods and operators to perform e.g. vector addition, scalar multiplication and compute it's length.
Details can be found in the [API reference][API#Vector2].

### `Debug`

You can display a string at any position in the game world by using the `DroneController.showText(text: String, position: Vector2)` method.
This is only valid for one timestep, so you if you will need to call this method on every timestep on which you want the text to be displayed.
E.g. if you wanted your drones to display their position, you could use this code:

    override def onTick(): Unit = {
      showText(position.toString, position)
    }

[API#DroneController]: http://codecraftgame.org/docs/api/index.html#cwinter.codecraft.core.api.DroneController
[API#Drone]: http://codecraftgame.org/docs/api/index.html#cwinter.codecraft.core.api.Drone
[API#Vector2]: http://codecraftgame.org/docs/api/index.html#cwinter.codecraft.core.api.Vector2

