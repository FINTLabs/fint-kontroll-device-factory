package no.novari.fintkontrolldevicefactory.service

import io.mockk.every
import io.mockk.mockk
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.ressurs.datautstyr.DigitalEnhetResource
import no.novari.cache.FintCache
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeviceServiceTest {

    private lateinit var deviceCache: FintCache<String, DigitalEnhetResource>
    private lateinit var linkedEntitiesService: LinkedEntitiesService
    private lateinit var deviceService: DeviceService

    @BeforeEach
    fun setup() {
        deviceCache = mockk()
        linkedEntitiesService = mockk()
        deviceService = DeviceService(deviceCache, linkedEntitiesService)
    }

    @Test
    fun `createDevice should create device with all fields populated`() {
        // Given
        val resource = createDigitalEnhetResource(
            systemId = "SYS123",
            serialNumber = "SN12345",
            dataObjectId = "DATA123",
            name = "Test Device",
            isPrivate = true,
            isShared = false
        )

        every { linkedEntitiesService.getDeviceTypeForDevice(resource) } returns "Laptop"
        every { linkedEntitiesService.getPlatformForDevice(resource) } returns "Windows"
        every { linkedEntitiesService.getStatusForDevice(resource) } returns "ACTIVE"
        every { linkedEntitiesService.getAdministratorIdForDevice(resource) } returns "admin123"
        every { linkedEntitiesService.getOwnerOrgUnitIdForDevice(resource) } returns "org123"

        // When
        val result = deviceService.createDevice(resource)

        // Then
        assertNotNull(result)
        assertEquals("SYS123", result?.systemId)
        assertEquals("SN12345", result?.serialNumber)
        assertEquals("DATA123", result?.dataObjectId)
        assertEquals("Test Device", result?.name)
        assertEquals(true, result?.isPrivateProperty)
        assertEquals(false, result?.isShared)
        assertEquals("ACTIVE", result?.status)
        assertEquals("Laptop", result?.deviceType)
        assertEquals("Windows", result?.platform)
        assertEquals("admin123", result?.administratorOrgUnitId)
        assertEquals("org123", result?.ownerOrgUnitId)
    }

    @Test
    fun `getAllDevices should return list of valid devices and filter out invalid ones`() {
        // Given
        val validDevice1 = createDigitalEnhetResource("device1", "SN001", "DATA001", "Device 1")
        val invalidDevice = createDigitalEnhetResource("device2", "SN002", "DATA002", "Device 2")
        val validDevice2 = createDigitalEnhetResource("device3", "SN003", "DATA003", "Device 3")

        every { deviceCache.getAll() } returns listOf(validDevice1, invalidDevice, validDevice2)
        
        // Valid devices have both deviceType and platform
        every { linkedEntitiesService.getDeviceTypeForDevice(validDevice1) } returns "Laptop"
        every { linkedEntitiesService.getPlatformForDevice(validDevice1) } returns "Windows"
        every { linkedEntitiesService.getStatusForDevice(validDevice1) } returns "ACTIVE"
        every { linkedEntitiesService.getAdministratorIdForDevice(validDevice1) } returns null
        every { linkedEntitiesService.getOwnerOrgUnitIdForDevice(validDevice1) } returns null
        
        // Invalid device missing platform
        every { linkedEntitiesService.getDeviceTypeForDevice(invalidDevice) } returns "Desktop"
        every { linkedEntitiesService.getPlatformForDevice(invalidDevice) } returns null
        
        every { linkedEntitiesService.getDeviceTypeForDevice(validDevice2) } returns "Tablet"
        every { linkedEntitiesService.getPlatformForDevice(validDevice2) } returns "iOS"
        every { linkedEntitiesService.getStatusForDevice(validDevice2) } returns "INACTIVE"
        every { linkedEntitiesService.getAdministratorIdForDevice(validDevice2) } returns null
        every { linkedEntitiesService.getOwnerOrgUnitIdForDevice(validDevice2) } returns null

        // When
        val result = deviceService.getAllDevices()

        // Then
        assertEquals(2, result.size)
        assertEquals("device1", result[0].systemId)
        assertEquals("device3", result[1].systemId)
    }

    @Test
    fun `createDevice should return null when deviceType is missing`() {
        // Given
        val resource = createDigitalEnhetResource("device1", "SN001", "DATA001", "Device 1")

        every { linkedEntitiesService.getDeviceTypeForDevice(resource) } returns null
        every { linkedEntitiesService.getPlatformForDevice(resource) } returns "Windows"

        // When
        val result = deviceService.createDevice(resource)

        // Then
        assertNull(result)
    }

    @Test
    fun `createDevice should return null when platform is missing`() {
        // Given
        val resource = createDigitalEnhetResource("device1", "SN001", "DATA001", "Device 1")

        every { linkedEntitiesService.getDeviceTypeForDevice(resource) } returns "Laptop"
        every { linkedEntitiesService.getPlatformForDevice(resource) } returns null

        // When
        val result = deviceService.createDevice(resource)

        // Then
        assertNull(result)
    }

    private fun createDigitalEnhetResource(
        systemId: String,
        serialNumber: String,
        dataObjectId: String,
        name: String,
        isPrivate: Boolean = false,
        isShared: Boolean = false
    ): DigitalEnhetResource {
        return DigitalEnhetResource().apply {
            this.systemId = Identifikator().apply { identifikatorverdi = systemId }
            this.serienummer = serialNumber
            this.dataobjektId = Identifikator().apply { identifikatorverdi = dataObjectId }
            this.navn = name
            this.privateid = isPrivate
            this.flerbrukerenhet = isShared
        }
    }
}