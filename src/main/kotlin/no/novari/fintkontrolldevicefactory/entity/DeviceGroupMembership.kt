package no.novari.fintkontrolldevicefactory.entity

data class DeviceGroupMembership (
    val groupId: String,
    val deviceId: String
){
     val id: String
         get() {
             return "${groupId}_${deviceId}"
         }
}