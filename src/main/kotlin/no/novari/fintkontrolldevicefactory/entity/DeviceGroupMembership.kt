package no.novari.fintkontrolldevicefactory.entity

import com.fasterxml.jackson.annotation.JsonIgnore

data class DeviceGroupMembership (
    val groupId: String,
    val deviceId: String
){
    @JsonIgnore
    fun getId(): String = "${deviceId}_${groupId}"
}