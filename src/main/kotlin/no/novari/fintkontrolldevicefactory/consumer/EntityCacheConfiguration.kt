package no.novari.fintkontrolldevicefactory.consumer

import no.fint.model.resource.ressurs.datautstyr.DigitalEnhetResource
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppeResource
import no.fint.model.resource.ressurs.kodeverk.EnhetstypeResource
import no.fint.model.resource.ressurs.kodeverk.EnhetstypeResources
import no.fint.model.resource.ressurs.kodeverk.PlattformResource
import no.fint.model.resource.ressurs.kodeverk.StatusResource
import no.fint.model.ressurs.datautstyr.DigitalEnhet
import no.fint.model.ressurs.datautstyr.Enhetsgruppe
import no.fint.model.ressurs.kodeverk.Enhetstype
import no.fint.model.ressurs.kodeverk.Plattform
import no.fint.model.ressurs.kodeverk.Status
import no.fintlabs.cache.FintCache
import no.fintlabs.cache.FintCacheManager
import no.novari.fintkontrolldevicefactory.entity.Device
import no.novari.fintkontrolldevicefactory.entity.DeviceGroup
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
    fun deviceResourceCache(): FintCache<String, DigitalEnhetResource> {
        return createCache(DigitalEnhetResource::class)
    }

    @Bean
    fun statusResourceCache(): FintCache<String, StatusResource> {
        return createCache(StatusResource::class)
    }


    @Bean
    fun cacheMap(
        platformCache: FintCache<String, PlattformResource>,
        deviceTypeCache: FintCache<String, EnhetstypeResource>,
        deviceGroupCache: FintCache<String, EnhetsgruppeResource>,
        deviceCache: FintCache<String, DigitalEnhetResource>,
        statusCache: FintCache<String, StatusResource>
    ): Map<KClass<*>, FintCache<String, *>> =
        mapOf(
            PlattformResource::class to platformCache,
            EnhetstypeResource::class to deviceTypeCache,
            EnhetsgruppeResource::class to deviceGroupCache,
            DigitalEnhetResource::class to deviceCache,
            StatusResource::class to statusCache
        )

    private fun <V : Any> createCache(resourceClass: KClass<V>): FintCache<String, V> {
        return fintCacheManager.createCache<String, V>(
            resourceClass.simpleName,
            String::class.java,
            resourceClass.java
        )
    }
}