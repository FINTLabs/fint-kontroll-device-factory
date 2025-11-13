package no.novari.fintkontrolldevicefactory.service

import io.github.oshai.kotlinlogging.KotlinLogging
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppeResource
import no.fintlabs.cache.FintCache
import no.novari.fintkontrolldevicefactory.entity.DeviceGroup
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class DeviceGroupService(
    val deviceGroupCache: FintCache<String, EnhetsgruppeResource>,
    val linkedEntitiesService: LinkedEntitiesService
) {

    fun getAllDeviceGroups(): List<DeviceGroup> {
        return deviceGroupCache.getAll().distinct().mapNotNull { createDeviceGroup(it) }
    }


    fun createDeviceGroup(deviceGroup: EnhetsgruppeResource): DeviceGroup? {
        val deviceType = linkedEntitiesService.getDeviceTypeForDeviceGroup(deviceGroup)
        val platform = linkedEntitiesService.getPlatformForDeviceGroup(deviceGroup)
        if (deviceType == null || platform == null) {
            logger.warn { "Skipping DeviceGroup ${deviceGroup.systemId}: missing deviceType or platform" }
            return null
        }
        return DeviceGroup(
            systemId = deviceGroup.systemId.identifikatorverdi,
            name = deviceGroup.navn,
            deviceType = deviceType,
            platform = platform,
            orgUnitId = linkedEntitiesService.getOrgUnitIdForDeviceGroup(deviceGroup),
        )
    }
}