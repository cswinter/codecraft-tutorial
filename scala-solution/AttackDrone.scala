import cwinter.codecraft.core.api._
import scala.util.Random

class AttackDrone extends DroneController {
  override def onTick(): Unit = {
    if (Random.nextInt(30) == 0) {
      val newDirection = 2 * math.Pi * Random.nextDouble()
      moveInDirection(newDirection)
    }
    if (weaponsCooldown <= 0) {
      for (drone <- dronesInSight) {
        if (drone.isEnemy && isInMissileRange(drone)) {
          shootMissiles(drone)
        }
      }
    }
  }
}
