package no.novari.fintkontrolldevicefactory.entity

data class DeviceGroup (
    val sourceId: String,
    val name: String,
    val orgUnitId: String?,
    val orgUnitName: String?,
    val platform: String,
    val deviceType: String,

    )