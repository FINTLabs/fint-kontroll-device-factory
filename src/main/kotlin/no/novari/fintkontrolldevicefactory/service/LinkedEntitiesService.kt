package no.novari.fintkontrolldevicefactory.service

import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource
import no.fint.model.resource.ressurs.datautstyr.DigitalEnhetResource
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppeResource
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppemedlemskapResource
import no.fint.model.resource.ressurs.kodeverk.EnhetstypeResource
import no.fint.model.resource.ressurs.kodeverk.PlattformResource
import no.fint.model.resource.ressurs.kodeverk.StatusResource
import no.novari.cache.FintCache
import no.novari.fintkontrolldevicefactory.LinkUtils
import no.novari.fintkontrolldevicefactory.entity.DeviceConfiguration
import org.springframework.stereotype.Service
import kotlin.collections.asSequence

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
        if (statusId.isNullOrBlank()) return "No ID found"
        val status: String =  mapToKontrollDeviceStatus(statusId, deviceConfiguration)

        return status
    }


    fun getStatus(systemId: String): String =
        if (isActiveStatus(systemId)) ACTIVE else INACTIVE

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



    private companion object {
        private const val ACTIVE = "ACTIVE"
        private const val INACTIVE = "INACTIVE"
        private const val INVALID = "INVALID"

    }
}
