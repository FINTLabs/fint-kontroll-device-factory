package no.novari.fintkontrolldevicefactory.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import no.fintlabs.cache.FintCache
import no.fintlabs.kafka.model.ParameterizedProducerRecord
import no.fintlabs.kafka.producing.ParameterizedTemplate
import no.fintlabs.kafka.producing.ParameterizedTemplateFactory
import no.fintlabs.kafka.topic.EntityTopicService
import no.fintlabs.kafka.topic.name.EntityTopicNameParameters
import no.novari.fintkontrolldevicefactory.entity.Device
import no.novari.fintkontrolldevicefactory.service.DeviceService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class DevicePublishingComponent(
    private val deviceService: DeviceService,
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    entityTopicService: EntityTopicService,
    private val deviceCache: FintCache<String, Device>
) {

    private val template: ParameterizedTemplate<Device> =
        parameterizedTemplateFactory.createTemplate(Device::class.java)

    private val nameParams: EntityTopicNameParameters =
        KafkaUtils.entityTopicNameParameters("device")

    init {
        entityTopicService.createOrModifyTopic(
            nameParams,
            KafkaUtils.entityTopicConfiguration()
        )
    }

    @Scheduled(
        fixedDelayString = "\${fint.kontroll.publishing.fixed-delay:PT5M}",
        initialDelayString = "\${fint.kontroll.publishing.initial-delay:PT5M}"
    )
    fun publishAll() {
        val all = deviceService.getAllDevices()

        val toPublish = all
            .mapNotNull { device ->
                val key = device.systemId
                val cached = deviceCache.getOptional(key).orElse(null)
                if (cached == null || cached != device) device else null
                //comment: what is compared here? Is it using the equals methode or should we be using a hash here?
            }
            .toList()


        toPublish.forEach { sendOne(it) }

        logger.info { "Found ${all.size} devices, published ${toPublish.size} out of them." }
    }

    private fun sendOne(device: Device) {
        val record = ParameterizedProducerRecord.builder<Device>()
            .topicNameParameters(nameParams)
            .key(device.systemId)
            .value(device)
            .build()

        template.send(record)
        logger.info { "Published device ${device.systemId}" }
    }
}
