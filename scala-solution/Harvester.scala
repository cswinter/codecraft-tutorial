import cwinter.codecraft.core.api._
import cwinter.codecraft.util.maths.Vector2
import scala.util.Random


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

