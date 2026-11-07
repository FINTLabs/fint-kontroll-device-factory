package no.novari.fintkontrolldevicefactory.entity

import no.novari.fintkontrolldevicefactory.service.mapToKontrollDeviceStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource

class DeviceConfigurationTest {
    @Test
    fun `OFK deployment values bind and map to device statuses`() {
        val properties = MapConfigurationPropertySource(
            mapOf(
                "novari.kontroll.status.active" to "9, 16, 101",
                "novari.kontroll.status.inactive" to "10, 11, 13, 15, 18, 102",
                "novari.kontroll.status.deleted" to "12, 14, 17, 100, 103",
            )
        )

        val configuration = Binder(properties).bind("novari.kontroll", DeviceConfiguration::class.java).get()

        assertEquals(listOf("9", "16", "101"), configuration.status.active)
        assertEquals(listOf("10", "11", "13", "15", "18", "102"), configuration.status.inactive)
        assertEquals(listOf("12", "14", "17", "100", "103"), configuration.status.deleted)

        configuration.status.active.forEach { assertEquals(DeviceStatus.ACTIVE, mapToKontrollDeviceStatus(it, configuration)) }
        configuration.status.inactive.forEach { assertEquals(DeviceStatus.INACTIVE, mapToKontrollDeviceStatus(it, configuration)) }
        configuration.status.deleted.forEach { assertEquals(DeviceStatus.DELETED, mapToKontrollDeviceStatus(it, configuration)) }
        assertEquals(DeviceStatus.INVALID, mapToKontrollDeviceStatus("unknown", configuration))
    }
}
