package no.novari.fintkontrolldevicefactory.entity

data class DeviceGroupMembership (
    val groupId: String,
    val deviceId: String
){
    fun getId(): String = "${deviceId}_${groupId}"
}