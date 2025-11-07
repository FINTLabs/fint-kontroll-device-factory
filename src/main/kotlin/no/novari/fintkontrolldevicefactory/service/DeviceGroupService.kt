package no.novari.fintkontrolldevicefactory.service

import no.fint.model.resource.ressurs.datautstyr.DigitalEnhetResource
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppeResource
import no.fint.model.resource.ressurs.kodeverk.EnhetstypeResource
import no.fintlabs.cache.FintCache
import no.novari.fintkontrolldevicefactory.entity.Device
import no.novari.fintkontrolldevicefactory.entity.DeviceGroup
import org.springframework.stereotype.Service

@Service
class DeviceGroupService(
    val deviceGroupCache: FintCache<String, EnhetsgruppeResource>,
    val linkedEntitiesService: LinkedEntitiesService
) {

    fun getAllDeviceGroups(): List<DeviceGroup> {
        return deviceGroupCache.getAll().distinct().map { createDeviceGroup(it) }
    }


    fun createDeviceGroup(deviceGroup: EnhetsgruppeResource): DeviceGroup {
        val deviceType = linkedEntitiesService.getDeviceTypeForDeviceGroup(deviceGroup)
        val platform = linkedEntitiesService.getPlatformForDeviceGroup(deviceGroup)
        return DeviceGroup(
            systemId = deviceGroup.systemId.toString(),
            name = deviceGroup.navn,
            deviceType = deviceType!!,
            platform = platform!!,
            orgUnitId = linkedEntitiesService.getOrgUnitIdForDeviceGroup(deviceGroup),
        )
    }
}