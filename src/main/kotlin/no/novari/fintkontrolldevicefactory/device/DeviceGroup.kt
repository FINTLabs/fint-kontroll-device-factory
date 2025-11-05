package no.novari.fintkontrolldevicefactory.device

data class DeviceGroup (
    val systemId: String,
    val name: String,
    val orgUnitId: String?,
    val platform: String,
    val deviceType: String,

)