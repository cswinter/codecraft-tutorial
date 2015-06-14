import cwinter.codecraft.core.api._


class Mothership extends DroneController {
  final val HarvesterSpec = new DroneSpec(storageModules = 2)
  final val CombatSpec = new DroneSpec(missileBatteries = 2, engines = 1, shieldGenerators = 1)
  private var harvesterCount = 0

  override def onTick(): Unit = {
    if (!isConstructing) {
      if (harvesterCount < 3) {
        buildDrone(HarvesterSpec, new HarvesterController(this))
        harvesterCount += 1
      } else {
        buildDrone(CombatSpec, new AttackDrone())
      }
    }
  }
}
