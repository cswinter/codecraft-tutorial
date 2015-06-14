import cwinter.codecraft.core.api._
import scala.util.Random


class HarvesterController(val mothership: DroneController) extends DroneController {
  private var isScouting = true

  override def onTick(): Unit = {
    if (isScouting && Random.nextInt(30) == 0) {
      val newDirection = 2 * math.Pi * Random.nextDouble()
      moveInDirection(newDirection)
    }
  }

  override def onMineralEntersVision(mineral: MineralCrystal): Unit = {
    if (isScouting) {
      moveTo(mineral)
      isScouting = false
    }
  }


  override def onArrivesAtMineral(mineral: MineralCrystal): Unit = {
    harvest(mineral)
    moveTo(mothership)
  }


  override def onArrivesAtDrone(drone: Drone): Unit = {
    giveMineralsTo(mothership)
    isScouting = true
  }
}

