package no.novari.fintkontrolldevicefactory.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.resource.ressurs.datautstyr.EnhetsgruppemedlemskapResource
import no.novari.cache.FintCache
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MembershipServiceTest {

    private lateinit var membershipCache: FintCache<String, EnhetsgruppemedlemskapResource>
    private lateinit var linkedEntitiesService: LinkedEntitiesService
    private lateinit var membershipService: MembershipService

    @BeforeEach
    fun setup() {
        membershipCache = mockk()
        linkedEntitiesService = mockk()
        membershipService = MembershipService(membershipCache, linkedEntitiesService)
    }

    @Test
    fun `getAllMemberships should return list of memberships`() {
        // Given
        val resource1 = createMembershipResource("1")
        val resource2 = createMembershipResource("2")

        every { membershipCache.getAll() } returns listOf(resource1, resource2)
        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource1) } returns "group1"
        every { linkedEntitiesService.getDeviceIdForMembership(resource1) } returns "device1"
        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource2) } returns "group2"
        every { linkedEntitiesService.getDeviceIdForMembership(resource2) } returns "device2"

        // When
        val result = membershipService.getAllMemberships()

        // Then
        assertEquals(2, result.size)
        assertEquals("group1", result[0].deviceGroupId)
        assertEquals("device1", result[0].deviceId)
        assertEquals("group2", result[1].deviceGroupId)
        assertEquals("device2", result[1].deviceId)
    }

    @Test
    fun `getAllMemberships should filter out memberships with missing deviceGroupId`() {
        // Given
        val resource1 = createMembershipResource("1")
        val resource2 = createMembershipResource("2")

        every { membershipCache.getAll() } returns listOf(resource1, resource2)
        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource1) } returns null
        every { linkedEntitiesService.getDeviceIdForMembership(resource1) } returns "device1"
        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource2) } returns "group2"
        every { linkedEntitiesService.getDeviceIdForMembership(resource2) } returns "device2"

        // When
        val result = membershipService.getAllMemberships()

        // Then
        assertEquals(1, result.size)
        assertEquals("group2", result[0].deviceGroupId)
        assertEquals("device2", result[0].deviceId)
    }

    @Test
    fun `getAllMemberships should filter out memberships with missing deviceId`() {
        // Given
        val resource1 = createMembershipResource("1")
        val resource2 = createMembershipResource("2")

        every { membershipCache.getAll() } returns listOf(resource1, resource2)
        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource1) } returns "group1"
        every { linkedEntitiesService.getDeviceIdForMembership(resource1) } returns null
        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource2) } returns "group2"
        every { linkedEntitiesService.getDeviceIdForMembership(resource2) } returns "device2"

        // When
        val result = membershipService.getAllMemberships()

        // Then
        assertEquals(1, result.size)
        assertEquals("group2", result[0].deviceGroupId)
        assertEquals("device2", result[0].deviceId)
    }

    @Test
    fun `getAllMemberships should filter out memberships with both missing deviceId and deviceGroupId`() {
        // Given
        val resource1 = createMembershipResource("1")
        val resource2 = createMembershipResource("2")

        every { membershipCache.getAll() } returns listOf(resource1, resource2)
        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource1) } returns null
        every { linkedEntitiesService.getDeviceIdForMembership(resource1) } returns null
        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource2) } returns "group2"
        every { linkedEntitiesService.getDeviceIdForMembership(resource2) } returns "device2"

        // When
        val result = membershipService.getAllMemberships()

        // Then
        assertEquals(1, result.size)
        assertEquals("group2", result[0].deviceGroupId)
    }

    @Test
    fun `getAllMemberships should handle empty cache`() {
        // Given
        every { membershipCache.getAll() } returns emptyList()

        // When
        val result = membershipService.getAllMemberships()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `getAllMemberships should handle duplicates`() {
        // Given
        val resource = createMembershipResource("1")

        every { membershipCache.getAll() } returns listOf(resource, resource)
        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource) } returns "group1"
        every { linkedEntitiesService.getDeviceIdForMembership(resource) } returns "device1"

        // When
        val result = membershipService.getAllMemberships()

        // Then
        assertEquals(1, result.size)
    }

    @Test
    fun `createMembership should create membership with valid data`() {
        // Given
        val resource = createMembershipResource("123")

        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource) } returns "group123"
        every { linkedEntitiesService.getDeviceIdForMembership(resource) } returns "device123"

        // When
        val result = membershipService.createMembership(resource)

        // Then
        assertNotNull(result)
        assertEquals("group123", result?.deviceGroupId)
        assertEquals("device123", result?.deviceId)
        assertEquals("123", result?.sourceId)
    }

    @Test
    fun `createMembership should return null when deviceGroupId is missing`() {
        // Given
        val resource = createMembershipResource("123")

        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource) } returns null
        every { linkedEntitiesService.getDeviceIdForMembership(resource) } returns "device123"

        // When
        val result = membershipService.createMembership(resource)

        // Then
        assertNull(result)
    }

    @Test
    fun `createMembership should return null when deviceId is missing`() {
        // Given
        val resource = createMembershipResource("123")

        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource) } returns "group123"
        every { linkedEntitiesService.getDeviceIdForMembership(resource) } returns null

        // When
        val result = membershipService.createMembership(resource)

        // Then
        assertNull(result)
    }

    @Test
    fun `createMembership should return null when both deviceId and deviceGroupId are missing`() {
        // Given
        val resource = createMembershipResource("123")

        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource) } returns null
        every { linkedEntitiesService.getDeviceIdForMembership(resource) } returns null

        // When
        val result = membershipService.createMembership(resource)

        // Then
        assertNull(result)
    }

    @Test
    fun `createMembership should verify all service calls`() {
        // Given
        val resource = createMembershipResource("456")

        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource) } returns "group456"
        every { linkedEntitiesService.getDeviceIdForMembership(resource) } returns "device456"

        // When
        membershipService.createMembership(resource)

        // Then
        verify(exactly = 1) { linkedEntitiesService.getDeviceGroupIdForMembership(resource) }
        verify(exactly = 1) { linkedEntitiesService.getDeviceIdForMembership(resource) }
    }

    @Test
    fun `getAllMemberships should call createMembership for each resource`() {
        // Given
        val resource1 = createMembershipResource("1")
        val resource2 = createMembershipResource("2")
        val resource3 = createMembershipResource("3")

        every { membershipCache.getAll() } returns listOf(resource1, resource2, resource3)
        every { linkedEntitiesService.getDeviceGroupIdForMembership(any()) } returns "group"
        every { linkedEntitiesService.getDeviceIdForMembership(any()) } returns "device"

        // When
        val result = membershipService.getAllMemberships()

        // Then
        assertEquals(3, result.size)
        verify(exactly = 3) { linkedEntitiesService.getDeviceGroupIdForMembership(any()) }
        verify(exactly = 3) { linkedEntitiesService.getDeviceIdForMembership(any()) }
    }

    @Test
    fun `getAllMemberships should filter out null results from createMembership`() {
        // Given
        val resource1 = createMembershipResource("1")
        val resource2 = createMembershipResource("2")
        val resource3 = createMembershipResource("3")

        every { membershipCache.getAll() } returns listOf(resource1, resource2, resource3)
        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource1) } returns "group1"
        every { linkedEntitiesService.getDeviceIdForMembership(resource1) } returns "device1"
        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource2) } returns null
        every { linkedEntitiesService.getDeviceIdForMembership(resource2) } returns "device2"
        every { linkedEntitiesService.getDeviceGroupIdForMembership(resource3) } returns "group3"
        every { linkedEntitiesService.getDeviceIdForMembership(resource3) } returns "device3"

        // When
        val result = membershipService.getAllMemberships()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.none { it.deviceGroupId == "null" })
    }

    private fun createMembershipResource(systemId: String): EnhetsgruppemedlemskapResource {
        return EnhetsgruppemedlemskapResource().apply {
            this.systemId = Identifikator(). apply { identifikatorverdi = systemId }
        }
    }
}