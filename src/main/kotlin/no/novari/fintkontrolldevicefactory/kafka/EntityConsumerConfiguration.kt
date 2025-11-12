package no.novari.fintkontrolldevicefactory.kafka

import no.fint.model.resource.ressurs.datautstyr.DigitalEnhetResource
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppeResource
import no.fint.model.resource.ressurs.datautstyr.EnhetsgruppemedlemskapResource
import no.fint.model.resource.ressurs.kodeverk.EnhetstypeResource
import no.fint.model.resource.ressurs.kodeverk.PlattformResource
import no.fint.model.resource.ressurs.kodeverk.StatusResource
import no.fintlabs.kafka.consuming.ListenerConfiguration
import no.fintlabs.kafka.consuming.ParameterizedListenerContainerFactoryService
import no.novari.fintkontrolldevicefactory.LinkUtils
import no.novari.fintkontrolldevicefactory.entity.Device
import no.novari.fintkontrolldevicefactory.entity.DeviceGroup
import no.novari.fintkontrolldevicefactory.entity.DeviceGroupMembership
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import kotlin.reflect.KClass


@Configuration
class EntityConsumerConfiguration(
    private val cacheWriterService: CacheWriterService,
    private val parameterizedListenerContainerFactoryService: ParameterizedListenerContainerFactoryService,
    private val kafkaErrorHandler: CommonErrorHandler,
) {

    @Bean
    fun deviceTypeConsumer() = createContainer(
        "ressurs-kodeverk-enhetstype", EnhetstypeResource::class
    )

    @Bean
    fun deviceConsumer() = createContainer(
        "ressurs-datautstyr-digitalenhet", DigitalEnhetResource::class
    )

    @Bean
    fun platformConsumer() = createContainer("ressurs-kodeverk-plattform", PlattformResource::class)

    @Bean
    fun deviceGroupConsumer() = createContainer("ressurs-datautstyr-enhetsgruppe", EnhetsgruppeResource::class)

    @Bean
    fun statusConsumer() = createContainer("ressurs-kodeverk-status", StatusResource::class)
    @Bean
    fun membershipConsumer() = createContainer("ressurs-datautstyr-enhetsgruppemedlemskap", EnhetsgruppemedlemskapResource::class)

    @Bean
    fun kontrollDeviceConsumer() = createContainer("device", Device::class)

    @Bean
    fun kontrollDeviceGroupConsumer() = createContainer("device-group", DeviceGroup::class)

    @Bean
    fun kontrollMembershipConsumer() = createContainer("device-group-membership", DeviceGroupMembership::class)


    private fun listenerConfiguration() = ListenerConfiguration.builder()
        .seekingOffsetResetOnAssignment(true)
        .maxPollRecords(10) // TODO check
        .build()


    private fun <T : Any> createContainer(resourceName: String, kclass: KClass<T>)
            : ConcurrentMessageListenerContainer<String, T> {

        val nameParams = KafkaUtils.entityTopicNameParameters(resourceName)

        val factory = parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
            kclass.java,
            { record: ConsumerRecord<String, T> ->

                cacheWriterService.putIntoCache(LinkUtils.getSystemIdFromMessageKey(record.key()), record.value(), kclass)
            },
            listenerConfiguration(),
            kafkaErrorHandler
        )

        return factory.createContainer(nameParams).apply { isAutoStartup = true }
    }


}