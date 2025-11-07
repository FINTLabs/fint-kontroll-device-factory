package no.novari.fintkontrolldevicefactory.service

import no.fint.model.resource.ressurs.datautstyr.DigitalEnhetResource
import no.fint.model.resource.ressurs.kodeverk.EnhetstypeResource
import no.fintlabs.cache.FintCache
import no.novari.fintkontrolldevicefactory.entity.Device
import org.springframework.stereotype.Service

@Service
class DeviceService(
    val deviceCache: FintCache<String, DigitalEnhetResource>,
    val linkedEntitiesService: LinkedEntitiesService
) {

    fun getAllDevices(): List<Device> {
        return deviceCache.getAll().distinct().map { createDevice(it) }
    }

    fun createDevice(digitalEnhet: DigitalEnhetResource): Device {
        val deviceType = linkedEntitiesService.getDeviceTypeForDevice(digitalEnhet)
        val platform = linkedEntitiesService.getPlatformForDevice(digitalEnhet)
        return Device(
            systemId = digitalEnhet.systemId.toString(),
            serialNumber = digitalEnhet.serienummer,
            dataObjectId = digitalEnhet.dataobjektId.toString(),
            name = digitalEnhet.navn,
            isPrivateProperty = digitalEnhet.privateid,
            isShared = digitalEnhet.flerbrukerenhet,
            status = linkedEntitiesService.getStatusForDevice(digitalEnhet),
            deviceType = deviceType!!,
            platform = platform!!,
            administratorOrgUnitId = linkedEntitiesService.getAdministratorIdForDevice(digitalEnhet),
            ownerOrgUnitId = linkedEntitiesService.getOwnerOrgUnitIdForDevice(digitalEnhet),
        )
    }
}