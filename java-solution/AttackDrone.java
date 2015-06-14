import cwinter.codecraft.core.api.*;
import java.util.Random;

class AttackDrone extends JDroneController {
  static Random rng = new Random();


  @Override public void onTick() {
    if (rng.nextInt(30) == 0) {
      Double newDirection = 2 * Math.PI * rng.nextDouble();
      moveInDirection(newDirection);
    }
    if (weaponsCooldown() <= 0) {
      for (Drone drone : dronesInSight()) {
        if (drone.isEnemy() && isInMissileRange(drone)) {
          shootMissiles(drone);
        }
      }
    }
  }
}
