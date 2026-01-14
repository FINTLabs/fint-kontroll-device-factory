package no.novari.fintkontrolldevicefactory.entity

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(prefix = "novari.kontroll.status")
data class DeviceConfiguration(
    var deviceStatus: DeviceStatus = DeviceStatus()
) {
    data class DeviceStatus(
        var active:  List<String> = emptyList(),
        var inactive : List<String> = emptyList(),
        var deleted : List<String> = emptyList()
    )
}
