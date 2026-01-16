package no.novari.fintkontrolldevicefactory.entity

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(prefix = "novari.kontroll")
data class DeviceConfiguration(
    var status: DeviceStatus = DeviceStatus()
) {
    data class DeviceStatus(
        var active:  List<String> = emptyList(),
        var inactive : List<String> = emptyList(),
        var deleted : List<String> = emptyList()
    )
}
