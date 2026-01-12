
package no.novari.fintkontrolldevicefactory.kafka

import io.mockk.*
import no.novari.cache.FintCache
import no.novari.kafka.producing.ParameterizedTemplate
import no.novari.kafka.producing.ParameterizedTemplateFactory
import no.novari.kafka.topic.EntityTopicService
import no.novari.fintkontrolldevicefactory.entity.DeviceGroup
import no.novari.fintkontrolldevicefactory.service.DeviceGroupService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class DeviceGroupPublishingComponentTest {

    private lateinit var deviceGroupService: DeviceGroupService
    private lateinit var parameterizedTemplateFactory: ParameterizedTemplateFactory
    private lateinit var entityTopicService: EntityTopicService
    private lateinit var kontrollDeviceGroupCache: FintCache<String, DeviceGroup>
    private lateinit var template: ParameterizedTemplate<DeviceGroup>
    private lateinit var publishingComponent: DeviceGroupPublishingComponent

    @BeforeEach
    fun setup() {
        deviceGroupService = mockk()
        parameterizedTemplateFactory = mockk()
        entityTopicService = mockk()
        kontrollDeviceGroupCache = mockk()
        template = mockk(relaxed = true)

        every { parameterizedTemplateFactory.createTemplate(DeviceGroup::class.java) } returns template
        every { entityTopicService.createOrModifyTopic(any(), any()) } returns Unit

        publishingComponent = DeviceGroupPublishingComponent(
            deviceGroupService,
            parameterizedTemplateFactory,
            entityTopicService,
            kontrollDeviceGroupCache
        )
    }

    @Test
    fun `publishAll should publish device groups that are not in cache`() {
        // Given
        val deviceGroup1 = createDeviceGroup("group1", "Group 1")
        val deviceGroup2 = createDeviceGroup("group2", "Group 2")

        every { deviceGroupService.getAllDeviceGroups() } returns listOf(deviceGroup1, deviceGroup2)
        every { kontrollDeviceGroupCache.getOptional("group1") } returns Optional.empty()
        every { kontrollDeviceGroupCache.getOptional("group2") } returns Optional.empty()

        // When
        publishingComponent.publishAll()

        // Then
        verify(exactly = 2) { template.send(any()) }
    }

    @Test
    fun `publishAll should publish device groups that have changed`() {
        // Given
        val oldDeviceGroup = createDeviceGroup("group1", "Old Name")
        val newDeviceGroup = createDeviceGroup("group1", "New Name")

        every { deviceGroupService.getAllDeviceGroups() } returns listOf(newDeviceGroup)
        every { kontrollDeviceGroupCache.getOptional("group1") } returns Optional.of(oldDeviceGroup)

        // When
        publishingComponent.publishAll()

        // Then
        verify(exactly = 1) { template.send(any()) }
    }

    @Test
    fun `publishAll should not publish device groups that are unchanged in cache`() {
        // Given
        val deviceGroup = createDeviceGroup("group1", "Group 1")

        every { deviceGroupService.getAllDeviceGroups() } returns listOf(deviceGroup)
        every { kontrollDeviceGroupCache.getOptional("group1") } returns Optional.of(deviceGroup)

        // When
        publishingComponent.publishAll()

        // Then
        verify(exactly = 0) { template.send(any()) }
    }

    @Test
    fun `publishAll should handle mixed scenarios - new, changed, and unchanged`() {
        // Given
        val newGroup = createDeviceGroup("group1", "New Group")
        val unchangedGroup = createDeviceGroup("group2", "Unchanged")
        val oldVersionOfChangedGroup = createDeviceGroup("group3", "Old Name")
        val changedGroup = createDeviceGroup("group3", "New Name")

        every { deviceGroupService.getAllDeviceGroups() } returns listOf(newGroup, unchangedGroup, changedGroup)
        every { kontrollDeviceGroupCache.getOptional("group1") } returns Optional.empty()
        every { kontrollDeviceGroupCache.getOptional("group2") } returns Optional.of(unchangedGroup)
        every { kontrollDeviceGroupCache.getOptional("group3") } returns Optional.of(oldVersionOfChangedGroup)

        // When
        publishingComponent.publishAll()

        // Then
        verify(exactly = 2) { template.send(any()) }
    }

    @Test
    fun `publishAll should handle empty device groups list`() {
        // Given
        every { deviceGroupService.getAllDeviceGroups() } returns emptyList()

        // When
        publishingComponent.publishAll()

        // Then
        verify(exactly = 0) { template.send(any()) }
    }

    @Test
    fun `publishAll should check cache for each device group`() {
        // Given
        val deviceGroup1 = createDeviceGroup("group1", "Group 1")
        val deviceGroup2 = createDeviceGroup("group2", "Group 2")
        val deviceGroup3 = createDeviceGroup("group3", "Group 3")

        every { deviceGroupService.getAllDeviceGroups() } returns listOf(deviceGroup1, deviceGroup2, deviceGroup3)
        every { kontrollDeviceGroupCache.getOptional(any()) } returns Optional.empty()

        // When
        publishingComponent.publishAll()

        // Then
        verify(exactly = 1) { kontrollDeviceGroupCache.getOptional("group1") }
        verify(exactly = 1) { kontrollDeviceGroupCache.getOptional("group2") }
        verify(exactly = 1) { kontrollDeviceGroupCache.getOptional("group3") }
    }

    private fun createDeviceGroup(
        systemId: String,
        name: String,
        deviceType: String = "Laptop",
        platform: String = "Windows",
        orgUnitId: String? = null,
        orgUnitName: String? = null,
    ): DeviceGroup {
        return DeviceGroup(
            systemId = systemId,
            name = name,
            deviceType = deviceType,
            platform = platform,
            orgUnitId = orgUnitId,
            orgUnitName = orgUnitName
        )
    }
}