package no.novari.fintkontrolldevicefactory.kafka

import no.fint.model.resource.ressurs.datautstyr.DigitalEnhetResource
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppeResource
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppemedlemskapResource
import no.fint.model.resource.ressurs.kodeverk.EnhetstypeResource
import no.fint.model.resource.ressurs.kodeverk.PlattformResource
import no.fint.model.resource.ressurs.kodeverk.StatusResource
import no.novari.cache.FintCache
import no.novari.cache.FintCacheManager
import no.novari.fintkontrolldevicefactory.entity.Device
import no.novari.fintkontrolldevicefactory.entity.DeviceGroup
import no.novari.fintkontrolldevicefactory.entity.DeviceGroupMembership
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.reflect.KClass

@Configuration
class EntityCacheConfiguration(
    private val fintCacheManager: FintCacheManager
) {
    @Bean
    fun platformResourceCache(): FintCache<String, PlattformResource> {
        return createCache(PlattformResource::class)
    }

    @Bean
    fun deviceTypeResourceCache(): FintCache<String, EnhetstypeResource> {
        return createCache(EnhetstypeResource::class)
    }

    @Bean
    fun deviceGroupResourceCache(): FintCache<String, EnhetsgruppeResource> {
        return createCache(EnhetsgruppeResource::class)
    }

    @Bean
    fun deviceGroupCache(): FintCache<String, DeviceGroup> {
        return createCache(DeviceGroup::class)
    }

    @Bean
    fun deviceCache(): FintCache<String, Device> {
        return createCache(Device::class)
    }

    @Bean
    fun membershipCache(): FintCache<String, DeviceGroupMembership> {
        return createCache(DeviceGroupMembership::class)
    }

    @Bean
    fun deviceResourceCache(): FintCache<String, DigitalEnhetResource> {
        return createCache(DigitalEnhetResource::class)
    }

    @Bean
    fun statusResourceCache(): FintCache<String, StatusResource> {
        return createCache(StatusResource::class)
    }

    @Bean
    fun membershipResourceCache(): FintCache<String, EnhetsgruppemedlemskapResource> {
        return createCache(EnhetsgruppemedlemskapResource::class)
    }


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
        deviceGroupCache: FintCache<String, DeviceGroup>
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
        )

    private fun <V : Any> createCache(resourceClass: KClass<V>): FintCache<String, V> {
        return fintCacheManager.createCache<String, V>(
            resourceClass.simpleName,
            String::class.java,
            resourceClass.java
        )
    }
}