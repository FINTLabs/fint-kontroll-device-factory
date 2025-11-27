package no.novari.fintkontrolldevicefactory.service

import io.mockk.every
import io.mockk.mockk
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.ressurs.datautstyr.DigitalEnhetResource
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppeResource
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppemedlemskapResource
import no.fint.model.resource.ressurs.kodeverk.EnhetstypeResource
import no.fint.model.resource.ressurs.kodeverk.PlattformResource
import no.fint.model.resource.ressurs.kodeverk.StatusResource
import no.novari.cache.FintCache
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class LinkedEntitiesServiceTest {

    private lateinit var deviceTypeResourceCache: FintCache<String, EnhetstypeResource>
    private lateinit var statusResourceCache: FintCache<String, StatusResource>
    private lateinit var platformResourceCache: FintCache<String, PlattformResource>
    private lateinit var linkedEntitiesService: LinkedEntitiesService

    @BeforeEach
    fun setup() {
        deviceTypeResourceCache = mockk()
        statusResourceCache = mockk()
        platformResourceCache = mockk()
        linkedEntitiesService = LinkedEntitiesService(
            deviceTypeResourceCache,
            statusResourceCache,
            platformResourceCache
        )
    }

    @Test
    fun `getStatusForDevice should return ACTIVE when device has active status`() {
        // Given
        val device = DigitalEnhetResource().apply {
            addStatus(Link.with("https://example.com/ressurs/status/status123"))
        }
        val statusResource = StatusResource().apply {
            navn = "ACTIVE"
        }

        every { statusResourceCache.getOptional("status123") } returns Optional.of(statusResource)

        // When
        val result = linkedEntitiesService.getStatusForDevice(device)

        // Then
        assertEquals("ACTIVE", result)
    }

    @Test
    fun `getStatus should return ACTIVE when systemId corresponds to active status`() {
        // Given
        val statusResource = StatusResource().apply {
            navn = "active"
        }

        every { statusResourceCache.getOptional("status456") } returns Optional.of(statusResource)

        // When
        val result = linkedEntitiesService.getStatus("status456")

        // Then
        assertEquals("ACTIVE", result)
    }

    @Test
    fun `getDeviceTypeForDevice should return device type name`() {
        // Given
        val device = DigitalEnhetResource().apply {
            addEnhetstype(Link.with("https://example.com/ressurs/enhetstype/type123"))
        }
        val deviceTypeResource = EnhetstypeResource().apply {
            navn = "Laptop"
        }

        every { deviceTypeResourceCache.getOptional("type123") } returns Optional.of(deviceTypeResource)

        // When
        val result = linkedEntitiesService.getDeviceTypeForDevice(device)

        // Then
        assertEquals("Laptop", result)
    }

    @Test
    fun `getPlatformForDevice should return platform name`() {
        // Given
        val device = DigitalEnhetResource().apply {
            addPlattform(Link.with("https://example.com/ressurs/plattform/platform123"))
        }
        val platformResource = PlattformResource().apply {
            navn = "Windows"
        }

        every { platformResourceCache.getOptional("platform123") } returns Optional.of(platformResource)

        // When
        val result = linkedEntitiesService.getPlatformForDevice(device)

        // Then
        assertEquals("Windows", result)
    }

    @Test
    fun `getAdministratorIdForDevice should return administrator ID`() {
        // Given
        val device = DigitalEnhetResource().apply {
            addAdministrator(Link.with("https://example.com/ressurs/organisasjonselement/admin123"))
        }

        // When
        val result = linkedEntitiesService.getAdministratorIdForDevice(device)

        // Then
        assertEquals("admin123", result)
    }

    @Test
    fun `getOwnerOrgUnitIdForDevice should return owner org unit ID`() {
        // Given
        val device = DigitalEnhetResource().apply {
            addEier(Link.with("https://example.com/ressurs/organisasjonselement/owner123"))
        }

        // When
        val result = linkedEntitiesService.getOwnerOrgUnitIdForDevice(device)

        // Then
        assertEquals("owner123", result)
    }

    @Test
    fun `getDeviceTypeForDeviceGroup should return device type name`() {
        // Given
        val deviceGroup = EnhetsgruppeResource().apply {
            systemId = Identifikator().apply { identifikatorverdi = "group1" }
            addEnhetstype(Link.with("https://example.com/ressurs/enhetstype/type456"))
        }
        val deviceTypeResource = EnhetstypeResource().apply {
            navn = "Desktop"
        }

        every { deviceTypeResourceCache.getOptional("type456") } returns Optional.of(deviceTypeResource)

        // When
        val result = linkedEntitiesService.getDeviceTypeForDeviceGroup(deviceGroup)

        // Then
        assertEquals("Desktop", result)
    }

    @Test
    fun `getPlatformForDeviceGroup should return platform name`() {
        // Given
        val deviceGroup = EnhetsgruppeResource().apply {
            systemId = Identifikator().apply { identifikatorverdi = "group1" }
            addPlattform(Link.with("https://example.com/ressurs/plattform/platform456"))
        }
        val platformResource = PlattformResource().apply {
            navn = "Linux"
        }

        every { platformResourceCache.getOptional("platform456") } returns Optional.of(platformResource)

        // When
        val result = linkedEntitiesService.getPlatformForDeviceGroup(deviceGroup)

        // Then
        assertEquals("Linux", result)
    }

    @Test
    fun `getOrgUnitIdForDeviceGroup should return org unit ID`() {
        // Given
        val deviceGroup = EnhetsgruppeResource().apply {
            systemId = Identifikator().apply { identifikatorverdi = "group1" }
            addOrganisasjonsenhet(Link.with("https://example.com/ressurs/organisasjonselement/org789"))
        }

        // When
        val result = linkedEntitiesService.getOrgUnitIdForDeviceGroup(deviceGroup)

        // Then
        assertEquals("org789", result)
    }

    @Test
    fun `getDeviceGroupIdForMembership should return device group ID`() {
        // Given
        val membership = EnhetsgruppemedlemskapResource().apply {
            addEnhetsgruppe(Link.with("https://example.com/ressurs/enhetsgruppe/group999"))
        }

        // When
        val result = linkedEntitiesService.getDeviceGroupIdForMembership(membership)

        // Then
        assertEquals("group999", result)
    }

    @Test
    fun `getDeviceIdForMembership should return device ID`() {
        // Given
        val membership = EnhetsgruppemedlemskapResource().apply {
            addDigitalEnhet(Link.with("https://example.com/ressurs/digitalenhet/device888"))
        }

        // When
        val result = linkedEntitiesService.getDeviceIdForMembership(membership)

        // Then
        assertEquals("device888", result)
    }
}