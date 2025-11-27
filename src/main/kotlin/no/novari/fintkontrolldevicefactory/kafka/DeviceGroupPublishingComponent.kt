package no.novari.fintkontrolldevicefactory.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import no.novari.cache.FintCache
import no.novari.kafka.producing.ParameterizedTemplate
import no.novari.kafka.producing.ParameterizedTemplateFactory
import no.novari.kafka.topic.EntityTopicService
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.fintkontrolldevicefactory.entity.DeviceGroup
import no.novari.fintkontrolldevicefactory.service.DeviceGroupService
import no.novari.kafka.producing.ParameterizedProducerRecord
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class DeviceGroupPublishingComponent(
    private val deviceGroupService: DeviceGroupService,
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    entityTopicService: EntityTopicService,
    private val kontrollDeviceGroupCache: FintCache<String, DeviceGroup>
) {

    private val template: ParameterizedTemplate<DeviceGroup> =
        parameterizedTemplateFactory.createTemplate(DeviceGroup::class.java)

    private val nameParams: EntityTopicNameParameters =
        KafkaUtils.entityTopicNameParameters("device-group")

    init {
        entityTopicService.createOrModifyTopic(
            nameParams,
            KafkaUtils.entityTopicConfiguration()
        )
    }

    @Scheduled(
        fixedDelayString = "\${novari.kontroll.publishing.fixed-delay:PT5M}",
        initialDelayString = "\${novari.kontroll.publishing.initial-delay:PT5M}"
    )
    fun publishAll() {
        val all = deviceGroupService.getAllDeviceGroups()

        val toPublish = all
            .mapNotNull { deviceGroup ->
                val key = deviceGroup.systemId
                val cached = kontrollDeviceGroupCache.getOptional(key).orElse(null)
                if (cached == null || cached != deviceGroup) deviceGroup else null
            }
            .toList()


        toPublish.forEach { sendOne(it) }

        logger.info { "Found ${all.size} device groups, published ${toPublish.size} out of them." }
    }

    private fun sendOne(deviceGroup: DeviceGroup) {
        val record = ParameterizedProducerRecord.builder<DeviceGroup>()
            .topicNameParameters(nameParams)
            .key(deviceGroup.systemId)
            .value(deviceGroup)
            .build()

        template.send(record)
        logger.info { "Published device-group ${deviceGroup.systemId}" }
    }
}
