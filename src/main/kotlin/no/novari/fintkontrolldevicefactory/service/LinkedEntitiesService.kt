package no.novari.fintkontrolldevicefactory.service

import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource
import no.novari.fint.model.resource.ressurs.datautstyr.DigitalEnhetResource
import no.novari.fint.model.resource.ressurs.datautstyr.EnhetsgruppeResource
import no.novari.fint.model.resource.ressurs.datautstyr.EnhetsgruppemedlemskapResource
import no.novari.fint.model.resource.ressurs.kodeverk.EnhetstypeResource
import no.novari.fint.model.resource.ressurs.kodeverk.PlattformResource
import no.novari.fint.model.resource.ressurs.kodeverk.StatusResource
import no.novari.cache.FintCache
import no.novari.fintkontrolldevicefactory.LinkUtils
import no.novari.fintkontrolldevicefactory.entity.DeviceConfiguration
import no.novari.fintkontrolldevicefactory.entity.DeviceStatus.ACTIVE
import no.novari.fintkontrolldevicefactory.entity.DeviceStatus.INACTIVE
import no.novari.fintkontrolldevicefactory.entity.DeviceStatus.INVALID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.collections.asSequence

private val logger = LoggerFactory.getLogger("LinkedEntitiesService")

@Service
class LinkedEntitiesService(
    private val deviceTypeResourceCache: FintCache<String, EnhetstypeResource>,
    private val statusResourceCache: FintCache<String, StatusResource>,
    private val platformResourceCache: FintCache<String, PlattformResource>,
    private val organisasjonselementResourceCache: FintCache<String, OrganisasjonselementResource>,
    private val deviceConfiguration: DeviceConfiguration

) {

    fun getStatusForDevice(device: DigitalEnhetResource): String{
        val statusId: String? = device.status.firstLinkedId()
        if (statusId.isNullOrBlank()) {
            logger.warn("Device:  ${device.systemId} has no reference to status")

            return INVALID
        }
        val status: String =  mapToKontrollDeviceStatus(statusId, deviceConfiguration)

        return status
    }


    fun getStatus(sourceId: String): String =
        if (isActiveStatus(sourceId)) ACTIVE else INACTIVE

    fun getDeviceTypeForDevice(device: DigitalEnhetResource): String? =
        device.enhetstype.firstResolved { id ->
            deviceTypeResourceCache.getOptional(id).map { it.navn }.orElse(null)
        }

    fun getPlatformForDevice(device: DigitalEnhetResource): String? =
        device.plattform.firstResolved { id ->
            platformResourceCache.getOptional(id).map { it.navn }.orElse(null)
        }

    fun getAdministratorIdForDevice(device: DigitalEnhetResource): String? =
        device.administrator.firstLinkedId()


    fun getOwnerOrgUnitIdForDevice(device: DigitalEnhetResource): String? =
        device.eier.firstLinkedId()

    fun getDeviceTypeForDeviceGroup(deviceGroup: EnhetsgruppeResource): String? =
        deviceGroup.enhetstype.firstResolved { id -> deviceTypeResourceCache.getOptional(id).map { it.navn }.orElse(null) }

    fun getPlatformForDeviceGroup(deviceGroup: EnhetsgruppeResource): String? =
        deviceGroup.plattform.firstResolved { id -> platformResourceCache.getOptional(id).map { it.navn }.orElse(null) }

    fun getOrgUnitIdForDeviceGroup(deviceGroup: EnhetsgruppeResource): String? =
        deviceGroup.organisasjonsenhet.firstLinkedId()

    fun getOrgUnitNameForDeviceGroup(deviceGroup: EnhetsgruppeResource): String? =
        deviceGroup.organisasjonsenhet.firstResolved { id -> organisasjonselementResourceCache.getOptional(id).map { it.navn }.orElse(null) }

    fun getDeviceGroupIdForMembership(membership: EnhetsgruppemedlemskapResource): String? =
        membership.enhetsgruppe.firstLinkedId()

    fun getDeviceIdForMembership(membership: EnhetsgruppemedlemskapResource): String? =
        membership.digitalEnhet.firstLinkedId()


    private fun isActiveStatus(systemId: String): Boolean =
        statusResourceCache
            .getOptional(systemId)
            .map { it.navn.equals(ACTIVE, ignoreCase = true) }
            .orElse(false)

    private fun Collection<Link>.firstLinkedId(): String? =
        asSequence()
            .map { LinkUtils.getSystemIdFromPath(it.href) }
            .firstOrNull()

    private inline fun <T> Collection<Link>.firstResolved(
        crossinline resolver: (String) -> T?
    ): T? =
        asSequence()
            .map { LinkUtils.getSystemIdFromPath(it.href) }
            .mapNotNull { resolver(it) }
            .firstOrNull()

    private inline fun Collection<Link>.anyLinked(
        crossinline predicate: (String) -> Boolean
    ): Boolean =
        asSequence()
            .map { LinkUtils.getSystemIdFromPath(it.href) }
            .any { predicate(it) }

}
