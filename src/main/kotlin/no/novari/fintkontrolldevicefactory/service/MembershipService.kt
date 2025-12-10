package no.novari.fintkontrolldevicefactory.service

import io.github.oshai.kotlinlogging.KotlinLogging
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppemedlemskapResource
import no.novari.cache.FintCache
import no.novari.fintkontrolldevicefactory.entity.DeviceGroupMembership
import org.springframework.stereotype.Service
private val logger = KotlinLogging.logger {}

@Service
class MembershipService(
    val membershipCache: FintCache<String, EnhetsgruppemedlemskapResource>,
    val linkedEntitiesService: LinkedEntitiesService
) {

    fun getAllMemberships(): List<DeviceGroupMembership> {
        return membershipCache.getAll().distinct().mapNotNull { createMembership(it) }
    }

    fun createMembership(membership: EnhetsgruppemedlemskapResource): DeviceGroupMembership? {
        val deviceGroupId = linkedEntitiesService.getDeviceGroupIdForMembership(membership)
        val deviceId = linkedEntitiesService.getDeviceIdForMembership(membership)
        if (deviceGroupId == null || deviceId == null) {
            logger.warn { "Skipping Membership ${membership.systemId}: missing deviceId or deviceGroupId" }
            return null
        }
        return DeviceGroupMembership(
            systemId = membership.systemId.identifikatorverdi,
            groupId = deviceGroupId,
            deviceId = deviceId
        )
    }
}