package no.novari.fintkontrolldevicefactory.consumer

import no.fint.model.resource.ressurs.datautstyr.DigitalEnhetResource
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppeResource
import no.fint.model.resource.ressurs.kodeverk.EnhetstypeResource
import no.fint.model.resource.ressurs.kodeverk.PlattformResource
import no.fint.model.resource.ressurs.kodeverk.StatusResource
import no.fintlabs.kafka.consuming.ListenerConfiguration
import no.fintlabs.kafka.consuming.ParameterizedListenerContainerFactoryService
import no.fintlabs.kafka.topic.name.EntityTopicNameParameters
import no.fintlabs.kafka.topic.name.TopicNamePrefixParameters
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.DefaultErrorHandler
import kotlin.reflect.KClass


@Configuration
class EntityConsumerConfiguration(
    private val cacheWriterService: CacheWriterService,
    private val parameterizedListenerContainerFactoryService: ParameterizedListenerContainerFactoryService
) {
    @Bean
    fun kafkaCommonErrorHandler(): CommonErrorHandler = DefaultErrorHandler()

    @Bean
    fun deviceTypeConsumer() = createContainer(
        "fint-kontroll-device-type", EnhetstypeResource::class
    )

    @Bean
    fun deviceConsumer() = createContainer(
        "fint-kontroll-device", DigitalEnhetResource::class
    )

    @Bean
    fun platformConsumer() = createContainer("fint-kontroll-platform", PlattformResource::class)

    @Bean
    fun deviceGroupConsumer() = createContainer("fint-kontroll-device-group", EnhetsgruppeResource::class)

    @Bean
    fun statusConsumer() = createContainer("fint-kontroll-status", StatusResource::class)

    private fun topicParams() = TopicNamePrefixParameters.builder()
        .orgIdApplicationDefault()
        .domainContextApplicationDefault()
        .build()

    private fun listenerConfiguration() = ListenerConfiguration.builder()
        .seekingOffsetResetOnAssignment(false)
        .maxPollRecords(10) // TODO check
        .build()


    private fun entityTopicNameParameters(resourceName: String) =
        EntityTopicNameParameters.builder()
            .resourceName(resourceName)
            .topicNamePrefixParameters(topicParams())
            .build()


    private fun <T : Any> createContainer(resourceName: String, kclass: KClass<T>)
            : ConcurrentMessageListenerContainer<String, T> {

        val nameParams = entityTopicNameParameters(resourceName)

        val factory = parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
            kclass.java,
            { record: ConsumerRecord<String, T> ->

                cacheWriterService.putIntoCache(record.key(), record.value(), kclass)
            },
            listenerConfiguration(),
            kafkaCommonErrorHandler()
        )

        return factory.createContainer(nameParams).apply { isAutoStartup = true }
    }


}