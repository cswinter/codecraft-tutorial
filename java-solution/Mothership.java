import cwinter.codecraft.core.api.*; 

class Mothership extends JDroneController {
  static final DroneSpec HARVESTER_SPEC = new DroneSpec(2, 0, 0, 0, 0, 0);
  static final DroneSpec COMBAT_SPEC = new DroneSpec(0, 2, 0, 0, 1, 1);
  int harvesters = 0;


  @Override public void onTick() {
    if (!isConstructing()) {
      if (harvesters < 3) {
        buildDrone(HARVESTER_SPEC, new HarvesterController(this)); 
        harvesters++;
      } else {
        buildDrone(COMBAT_SPEC, new AttackDrone());
      }
    }
  }
}
