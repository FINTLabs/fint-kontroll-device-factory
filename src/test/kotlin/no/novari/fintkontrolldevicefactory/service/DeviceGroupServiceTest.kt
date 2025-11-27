package no.novari.fintkontrolldevicefactory.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppeResource
import no.novari.cache.FintCache
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeviceGroupServiceTest {

    private lateinit var deviceGroupCache: FintCache<String, EnhetsgruppeResource>
    private lateinit var linkedEntitiesService: LinkedEntitiesService
    private lateinit var deviceGroupService: DeviceGroupService

    @BeforeEach
    fun setup() {
        deviceGroupCache = mockk()
        linkedEntitiesService = mockk()
        deviceGroupService = DeviceGroupService(deviceGroupCache, linkedEntitiesService)
    }

    @Test
    fun `getAllDeviceGroups should return list of device groups`() {
        // Given
        val resource1 = createEnhetsgruppeResource("1", "Group 1")
        val resource2 = createEnhetsgruppeResource("2", "Group 2")

        every { deviceGroupCache.getAll() } returns listOf(resource1, resource2)
        every { linkedEntitiesService.getDeviceTypeForDeviceGroup(resource1) } returns "Laptop"
        every { linkedEntitiesService.getPlatformForDeviceGroup(resource1) } returns "Windows"
        every { linkedEntitiesService.getOrgUnitIdForDeviceGroup(resource1) } returns "org1"
        every { linkedEntitiesService.getDeviceTypeForDeviceGroup(resource2) } returns "Desktop"
        every { linkedEntitiesService.getPlatformForDeviceGroup(resource2) } returns "Linux"
        every { linkedEntitiesService.getOrgUnitIdForDeviceGroup(resource2) } returns "org2"

        // When
        val result = deviceGroupService.getAllDeviceGroups()

        // Then
        assertEquals(2, result.size)
        assertEquals("1", result[0].systemId)
        assertEquals("Group 1", result[0].name)
        assertEquals("Laptop", result[0].deviceType)
        assertEquals("Windows", result[0].platform)
        assertEquals("org1", result[0].orgUnitId)
    }

    @Test
    fun `getAllDeviceGroups should filter out device groups with missing deviceType`() {
        // Given
        val resource1 = createEnhetsgruppeResource("1", "Group 1")
        val resource2 = createEnhetsgruppeResource("2", "Group 2")

        every { deviceGroupCache.getAll() } returns listOf(resource1, resource2)
        every { linkedEntitiesService.getDeviceTypeForDeviceGroup(resource1) } returns null
        every { linkedEntitiesService.getPlatformForDeviceGroup(resource1) } returns "Windows"
        every { linkedEntitiesService.getDeviceTypeForDeviceGroup(resource2) } returns "Desktop"
        every { linkedEntitiesService.getPlatformForDeviceGroup(resource2) } returns "Linux"
        every { linkedEntitiesService.getOrgUnitIdForDeviceGroup(resource2) } returns "org2"

        // When
        val result = deviceGroupService.getAllDeviceGroups()

        // Then
        assertEquals(1, result.size)
        assertEquals("2", result[0].systemId)
    }

    @Test
    fun `getAllDeviceGroups should filter out device groups with missing platform`() {
        // Given
        val resource1 = createEnhetsgruppeResource("1", "Group 1")
        val resource2 = createEnhetsgruppeResource("2", "Group 2")

        every { deviceGroupCache.getAll() } returns listOf(resource1, resource2)
        every { linkedEntitiesService.getDeviceTypeForDeviceGroup(resource1) } returns "Laptop"
        every { linkedEntitiesService.getPlatformForDeviceGroup(resource1) } returns null
        every { linkedEntitiesService.getDeviceTypeForDeviceGroup(resource2) } returns "Desktop"
        every { linkedEntitiesService.getPlatformForDeviceGroup(resource2) } returns "Linux"
        every { linkedEntitiesService.getOrgUnitIdForDeviceGroup(resource2) } returns "org2"

        // When
        val result = deviceGroupService.getAllDeviceGroups()

        // Then
        assertEquals(1, result.size)
        assertEquals("2", result[0].systemId)
    }

    @Test
    fun `getAllDeviceGroups should handle empty cache`() {
        // Given
        every { deviceGroupCache.getAll() } returns emptyList()

        // When
        val result = deviceGroupService.getAllDeviceGroups()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `getAllDeviceGroups should handle duplicates`() {
        // Given
        val resource = createEnhetsgruppeResource("1", "Group 1")

        every { deviceGroupCache.getAll() } returns listOf(resource, resource)
        every { linkedEntitiesService.getDeviceTypeForDeviceGroup(resource) } returns "Laptop"
        every { linkedEntitiesService.getPlatformForDeviceGroup(resource) } returns "Windows"
        every { linkedEntitiesService.getOrgUnitIdForDeviceGroup(resource) } returns "org1"

        // When
        val result = deviceGroupService.getAllDeviceGroups()

        // Then
        assertEquals(1, result.size)
    }

    @Test
    fun `createDeviceGroup should create device group with all fields`() {
        // Given
        val resource = createEnhetsgruppeResource("123", "Test Group")

        every { linkedEntitiesService.getDeviceTypeForDeviceGroup(resource) } returns "Tablet"
        every { linkedEntitiesService.getPlatformForDeviceGroup(resource) } returns "iOS"
        every { linkedEntitiesService.getOrgUnitIdForDeviceGroup(resource) } returns "org123"

        // When
        val result = deviceGroupService.createDeviceGroup(resource)

        // Then
        assertNotNull(result)
        assertEquals("123", result?.systemId)
        assertEquals("Test Group", result?.name)
        assertEquals("Tablet", result?.deviceType)
        assertEquals("iOS", result?.platform)
        assertEquals("org123", result?.orgUnitId)
    }

    @Test
    fun `createDeviceGroup should return null when deviceType is missing`() {
        // Given
        val resource = createEnhetsgruppeResource("123", "Test Group")

        every { linkedEntitiesService.getDeviceTypeForDeviceGroup(resource) } returns null
        every { linkedEntitiesService.getPlatformForDeviceGroup(resource) } returns "iOS"

        // When
        val result = deviceGroupService.createDeviceGroup(resource)

        // Then
        assertNull(result)
    }

    @Test
    fun `createDeviceGroup should return null when platform is missing`() {
        // Given
        val resource = createEnhetsgruppeResource("123", "Test Group")

        every { linkedEntitiesService.getDeviceTypeForDeviceGroup(resource) } returns "Tablet"
        every { linkedEntitiesService.getPlatformForDeviceGroup(resource) } returns null

        // When
        val result = deviceGroupService.createDeviceGroup(resource)

        // Then
        assertNull(result)
    }

    @Test
    fun `createDeviceGroup should handle null orgUnitId`() {
        // Given
        val resource = createEnhetsgruppeResource("123", "Test Group")

        every { linkedEntitiesService.getDeviceTypeForDeviceGroup(resource) } returns "Tablet"
        every { linkedEntitiesService.getPlatformForDeviceGroup(resource) } returns "iOS"
        every { linkedEntitiesService.getOrgUnitIdForDeviceGroup(resource) } returns null

        // When
        val result = deviceGroupService.createDeviceGroup(resource)

        // Then
        assertNotNull(result)
        assertNull(result?.orgUnitId)
    }

    @Test
    fun `createDeviceGroup should verify all service calls`() {
        // Given
        val resource = createEnhetsgruppeResource("456", "Verify Group")

        every { linkedEntitiesService.getDeviceTypeForDeviceGroup(resource) } returns "Phone"
        every { linkedEntitiesService.getPlatformForDeviceGroup(resource) } returns "Android"
        every { linkedEntitiesService.getOrgUnitIdForDeviceGroup(resource) } returns "org456"

        // When
        deviceGroupService.createDeviceGroup(resource)

        // Then
        verify(exactly = 1) { linkedEntitiesService.getDeviceTypeForDeviceGroup(resource) }
        verify(exactly = 1) { linkedEntitiesService.getPlatformForDeviceGroup(resource) }
        verify(exactly = 1) { linkedEntitiesService.getOrgUnitIdForDeviceGroup(resource) }
    }

    private fun createEnhetsgruppeResource(systemId: String, navn: String): EnhetsgruppeResource {
        return EnhetsgruppeResource().apply {
            this.systemId = Identifikator().apply { this.identifikatorverdi = systemId }
            this.navn = navn
        }
    }
}