import cwinter.codecraft.core.api.*;
import cwinter.codecraft.util.maths.Vector2;
import java.util.Random;

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
        Vector2 randomDirection = new Vector2(2 * Math.PI * rng.nextDouble());
        Vector2 targetPosition = position().plus(randomDirection.times(500));
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

