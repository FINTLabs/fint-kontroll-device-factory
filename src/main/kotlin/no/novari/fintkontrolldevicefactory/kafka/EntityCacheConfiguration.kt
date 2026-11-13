package no.novari.fintkontrolldevicefactory.kafka

import no.novari.cache.FintCache
import no.novari.cache.FintCacheManager
import no.novari.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource
import no.novari.fint.model.resource.ressurs.datautstyr.DigitalEnhetResource
import no.novari.fint.model.resource.ressurs.datautstyr.EnhetsgruppeResource
import no.novari.fint.model.resource.ressurs.datautstyr.EnhetsgruppemedlemskapResource
import no.novari.fint.model.resource.ressurs.kodeverk.EnhetstypeResource
import no.novari.fint.model.resource.ressurs.kodeverk.PlattformResource
import no.novari.fint.model.resource.ressurs.kodeverk.StatusResource
import no.novari.fintkontrolldevicefactory.entity.Device
import no.novari.fintkontrolldevicefactory.entity.DeviceGroup
import no.novari.fintkontrolldevicefactory.entity.DeviceGroupMembership
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.reflect.KClass

@Configuration
class EntityCacheConfiguration(
    private val fintCacheManager: FintCacheManager,
) {
    @Bean
    fun organisasjonselementCache(): FintCache<String, OrganisasjonselementResource> = createCache(OrganisasjonselementResource::class)

    @Bean
    fun platformResourceCache(): FintCache<String, PlattformResource> = createCache(PlattformResource::class)

    @Bean
    fun deviceTypeResourceCache(): FintCache<String, EnhetstypeResource> = createCache(EnhetstypeResource::class)

    @Bean
    fun deviceGroupResourceCache(): FintCache<String, EnhetsgruppeResource> = createCache(EnhetsgruppeResource::class)

    @Bean
    fun deviceGroupCache(): FintCache<String, DeviceGroup> = createCache(DeviceGroup::class)

    @Bean
    fun deviceCache(): FintCache<String, Device> = createCache(Device::class)

    @Bean
    fun membershipCache(): FintCache<String, DeviceGroupMembership> = createCache(DeviceGroupMembership::class)

    @Bean
    fun deviceResourceCache(): FintCache<String, DigitalEnhetResource> = createCache(DigitalEnhetResource::class)

    @Bean
    fun statusResourceCache(): FintCache<String, StatusResource> = createCache(StatusResource::class)

    @Bean
    fun membershipResourceCache(): FintCache<String, EnhetsgruppemedlemskapResource> = createCache(EnhetsgruppemedlemskapResource::class)

    @Bean
    fun cacheMap(
        platformCache: FintCache<String, PlattformResource>,
        deviceTypeCache: FintCache<String, EnhetstypeResource>,
        deviceGroupResourceCache: FintCache<String, EnhetsgruppeResource>,
        deviceResourceCache: FintCache<String, DigitalEnhetResource>,
        membershipResourceCache: FintCache<String, EnhetsgruppemedlemskapResource>,
        statusCache: FintCache<String, StatusResource>,
        membershipCache: FintCache<String, DeviceGroupMembership>,
        deviceCache: FintCache<String, Device>,
        deviceGroupCache: FintCache<String, DeviceGroup>,
        organisasjonselementCache: FintCache<String, OrganisasjonselementResource>,
    ): Map<KClass<*>, FintCache<String, *>> =
        mapOf(
            PlattformResource::class to platformCache,
            EnhetstypeResource::class to deviceTypeCache,
            EnhetsgruppeResource::class to deviceGroupResourceCache,
            DigitalEnhetResource::class to deviceResourceCache,
            EnhetsgruppemedlemskapResource::class to membershipResourceCache,
            StatusResource::class to statusCache,
            Device::class to deviceCache,
            DeviceGroup::class to deviceGroupCache,
            DeviceGroupMembership::class to membershipCache,
            OrganisasjonselementResource::class to organisasjonselementCache,
        )

    private fun <V : Any> createCache(resourceClass: KClass<V>): FintCache<String, V> =
        fintCacheManager.createCache<String, V>(
            resourceClass.simpleName,
            String::class.java,
            resourceClass.java,
        )
}
