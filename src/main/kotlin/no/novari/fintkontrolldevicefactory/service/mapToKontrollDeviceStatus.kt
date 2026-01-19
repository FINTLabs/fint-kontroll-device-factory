package no.novari.fintkontrolldevicefactory.service

import no.novari.fintkontrolldevicefactory.entity.DeviceConfiguration
import no.novari.fintkontrolldevicefactory.entity.DeviceStatus.ACTIVE
import no.novari.fintkontrolldevicefactory.entity.DeviceStatus.DELETED
import no.novari.fintkontrolldevicefactory.entity.DeviceStatus.INACTIVE
import no.novari.fintkontrolldevicefactory.entity.DeviceStatus.INVALID
import org.slf4j.LoggerFactory


private val logger = LoggerFactory.getLogger("DeviceStatusMappingService")

    fun mapToKontrollDeviceStatus(
        fintStatus: String,
        deviceConfiguration: DeviceConfiguration
    ): String {

        logger.info("Mapping from Kontroll device status StatusID: $fintStatus")
        if (fintStatus.isBlank()) {
            logger.warn("Device has no status")
            return INVALID
        }

        return when{
            deviceConfiguration.status.active.contains(fintStatus) -> {
                logger.debug("Device has status ACTIVE")
                ACTIVE
            }
            deviceConfiguration.status.inactive.contains(fintStatus) -> {
                logger.debug("Device has status INACTIVE")
                INACTIVE
            }
            deviceConfiguration.status.deleted.contains(fintStatus) -> {
                logger.debug("Device has status DELETED")
                DELETED
            }
            else -> {
                logger.warn("Device has status INVALID")
                INVALID
            }
        }
    }
