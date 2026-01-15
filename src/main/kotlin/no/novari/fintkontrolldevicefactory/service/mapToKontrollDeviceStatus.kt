package no.novari.fintkontrolldevicefactory.service

import no.novari.fintkontrolldevicefactory.entity.DeviceConfiguration
import org.slf4j.LoggerFactory


private val logger = LoggerFactory.getLogger("DeviceStatusMappingService")

    fun mapToKontrollDeviceStatus(
        fintStatus: String,
        deviceConfiguration: DeviceConfiguration
    ): String {

        logger.info("Mapping from Kontroll device status StatusID: $fintStatus")
        if (fintStatus.isBlank()) {
            logger.warn("Device has no status")
            return "NOT FOUND"
        }

        return when{
            deviceConfiguration.status.active.contains(fintStatus) -> {
                logger.debug("Device has status ACTIVE")
                "ACTIVE"
            }
            deviceConfiguration.status.inactive.contains(fintStatus) -> {
                logger.debug("Device has status INACTIVE")
                "INACTIVE"
            }
            deviceConfiguration.status.deleted.contains(fintStatus) -> {
                logger.debug("Device has status DELETED")
                "DELETED"
            }
            else -> "INVALID STATUS"
        }
    }
