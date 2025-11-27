package no.novari.fintkontrolldevicefactory.service

import io.github.oshai.kotlinlogging.KotlinLogging
import no.fint.model.resource.ressurs.datautstyr.DigitalEnhetResource
import no.novari.cache.FintCache
import no.novari.fintkontrolldevicefactory.entity.Device
import org.springframework.stereotype.Service
private val logger = KotlinLogging.logger {}

@Service
class DeviceService(
    val deviceCache: FintCache<String, DigitalEnhetResource>,
    val linkedEntitiesService: LinkedEntitiesService
) {

    fun getAllDevices(): List<Device> {
        return deviceCache.getAll().distinct().mapNotNull { createDevice(it) }
    }

    fun createDevice(device: DigitalEnhetResource): Device? {
        // TODO is there any extra condition  to say that the device is valid/invalid for us?
        val deviceType = linkedEntitiesService.getDeviceTypeForDevice(device)
        val platform = linkedEntitiesService.getPlatformForDevice(device)
        if (deviceType == null || platform == null) {
            logger.warn { "Skipping DeviceGroup ${device.systemId}: missing deviceType or platform" }
            return null
        }
        return Device(
            systemId = device.systemId.identifikatorverdi,
            serialNumber = device.serienummer,
            dataObjectId = device.dataobjektId.identifikatorverdi,
            name = device.navn,
            isPrivateProperty = device.privateid,
            isShared = device.flerbrukerenhet,
            status = linkedEntitiesService.getStatusForDevice(device),
            deviceType = deviceType,
            platform = platform,
            administratorOrgUnitId = linkedEntitiesService.getAdministratorIdForDevice(device),
            ownerOrgUnitId = linkedEntitiesService.getOwnerOrgUnitIdForDevice(device),
        )
    }
}