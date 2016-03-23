import cwinter.codecraft.core.api.*;
import cwinter.codecraft.util.maths.Vector2;
import java.util.Random;


class Soldier extends JDroneController {
  static Random rng = new Random();


  @Override public void onTick() {
    for (Drone drone : dronesInSight()) {
      if (drone.isEnemy()) {
        moveTo(drone.position());
        if (missileCooldown() == 0 && isInMissileRange(drone)) {
          fireMissilesAt(drone);
        }
        return;
      }
    }

    if (!isMoving()) {
      Vector2 randomDirection = new Vector2(2 * Math.PI * rng.nextDouble());
      Vector2 targetPosition = position().plus(randomDirection.times(500));
      moveTo(targetPosition);
    }
  }
}

