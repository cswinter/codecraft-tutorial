import cwinter.codecraft.core.api._


class Mothership extends DroneController {
  var nHarvesters = 0

  override def onTick(): Unit = {
    if (!isConstructing) {
      if (nHarvesters < 3) {
        buildDrone(new Harvester(this), storageModules = 2)
        nHarvesters += 1
      } else buildDrone(new Soldier, missileBatteries = 3, shieldGenerators = 1)
    }
  }
}

