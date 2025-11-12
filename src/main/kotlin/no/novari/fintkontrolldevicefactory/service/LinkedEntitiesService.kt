package no.novari.fintkontrolldevicefactory.service

import no.fint.model.resource.Link
import no.fint.model.resource.ressurs.datautstyr.DigitalEnhetResource
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppeResource
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppemedlemskapResource
import no.fint.model.resource.ressurs.kodeverk.EnhetstypeResource
import no.fint.model.resource.ressurs.kodeverk.PlattformResource
import no.fint.model.resource.ressurs.kodeverk.StatusResource
import no.fintlabs.cache.FintCache
import no.novari.fintkontrolldevicefactory.LinkUtils
import org.springframework.stereotype.Service
import kotlin.collections.asSequence

@Service
class LinkedEntitiesService(
    private val deviceTypeResourceCache: FintCache<String, EnhetstypeResource>,
    private val statusResourceCache: FintCache<String, StatusResource>,
    private val platformResourceCache: FintCache<String, PlattformResource>,
) {

    fun getStatusForDevice(device: DigitalEnhetResource): String =
        device.status
            .anyLinked { id -> isActiveStatus(id) }
            .let { if (it) ACTIVE else INACTIVE }

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
    }
}
