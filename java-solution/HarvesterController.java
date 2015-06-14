import cwinter.codecraft.core.api.*;
import java.util.Random;

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
  @Override public void onMineralEntersVision(MineralCrystal mineral) {
    if (isScouting) {
      moveTo(mineral);
      isScouting = false;
    }
  }

  // once we arrive at the mineral, harvest it and bring it back to the mothership
  @Override public void onArrivesAtMineral(MineralCrystal mineral) {
    harvest(mineral);
    moveTo(mothership);
  }

  // once we arrive at the mothership, deposit the crystal and switch back to scouting mode
  @Override public void onArrivesAtDrone(Drone drone) {
    giveMineralsTo(mothership);
    isScouting = true;
  }
}
