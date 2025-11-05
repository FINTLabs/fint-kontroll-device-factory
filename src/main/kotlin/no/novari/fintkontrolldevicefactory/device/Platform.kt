package no.novari.fintkontrolldevicefactory.device

data class Platform (
    val validityPeriod: Period?,
    val code: String,
    val name: String,
    val systemId: String,
)