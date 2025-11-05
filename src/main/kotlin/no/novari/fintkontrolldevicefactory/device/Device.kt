package no.novari.fintkontrolldevicefactory.device

data class Device (
    val systemId: String,
    val serialNumber: String,
    val dataObjectId: String?,
    val name: String?,
    val isPrivateProperty: Boolean?,
    val isShared: Boolean?,
    val status: String?,
    val deviceType: String,
    val platform: String,
    val administratorOrgUnitId: String?,
    val ownerOrgUnitId: String?,

)