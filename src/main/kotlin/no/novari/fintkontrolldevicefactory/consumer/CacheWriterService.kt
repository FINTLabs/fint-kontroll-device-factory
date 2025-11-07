package no.novari.fintkontrolldevicefactory.consumer

import no.fintlabs.cache.FintCache
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class CacheWriterService(
    private val cacheMap: Map<KClass<*>, FintCache<String, *>>
) {

    fun <T : Any> putIntoCache(key: String?, value: T, type: KClass<T>) {
        if (key == null) return

        val cache = cacheMap[type] as? FintCache<String, T>
            ?: throw IllegalArgumentException("No cache configured for type: ${type.simpleName}")

        cache.put(key, value)
        println("$key $value")
    }
}