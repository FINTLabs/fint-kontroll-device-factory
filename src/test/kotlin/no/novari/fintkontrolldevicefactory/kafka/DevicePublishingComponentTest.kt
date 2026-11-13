package no.novari.fintkontrolldevicefactory.kafka

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.novari.cache.FintCache
import no.novari.fintkontrolldevicefactory.entity.Device
import no.novari.fintkontrolldevicefactory.entity.DeviceStatus.INVALID
import no.novari.fintkontrolldevicefactory.service.DeviceService
import no.novari.kafka.producing.ParameterizedProducerRecord
import no.novari.kafka.producing.ParameterizedTemplate
import no.novari.kafka.producing.ParameterizedTemplateFactory
import no.novari.kafka.topic.EntityTopicService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Optional

class DevicePublishingComponentTest {
    @Test
    fun `publishAll should send a device with missing data object ID and INVALID status`() {
        val device =
            Device(
                sourceId = "device1",
                serialNumber = "SN001",
                dataObjectId = null,
                name = "Device 1",
                isPrivateProperty = false,
                isShared = false,
                status = INVALID,
                deviceType = "Laptop",
                platform = "Windows",
                administratorOrgUnitId = null,
                ownerOrgUnitId = null,
            )
        val deviceService = mockk<DeviceService>()
        val templateFactory = mockk<ParameterizedTemplateFactory>()
        val topicService = mockk<EntityTopicService>()
        val deviceCache = mockk<FintCache<String, Device>>()
        val template = mockk<ParameterizedTemplate<Device>>(relaxed = true)
        every { deviceService.getAllDevices() } returns listOf(device)
        every { templateFactory.createTemplate(Device::class.java) } returns template
        every { topicService.createOrModifyTopic(any(), any()) } returns Unit
        every { deviceCache.getOptional("device1") } returns Optional.empty()
        val publishingComponent = DevicePublishingComponent(deviceService, templateFactory, topicService, deviceCache)
        val sentRecord = slot<ParameterizedProducerRecord<Device>>()

        publishingComponent.publishAll()

        verify(exactly = 1) { template.send(capture(sentRecord)) }
        assertEquals("device1", sentRecord.captured.key)
        assertEquals(device, sentRecord.captured.value)
    }
}
