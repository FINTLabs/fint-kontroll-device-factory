package no.novari.fintkontrolldevicefactory.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import no.fintlabs.cache.FintCache
import no.fintlabs.kafka.model.ParameterizedProducerRecord
import no.fintlabs.kafka.producing.ParameterizedTemplate
import no.fintlabs.kafka.producing.ParameterizedTemplateFactory
import no.fintlabs.kafka.topic.EntityTopicService
import no.fintlabs.kafka.topic.name.EntityTopicNameParameters
import no.novari.fintkontrolldevicefactory.entity.DeviceGroup
import no.novari.fintkontrolldevicefactory.service.DeviceGroupService
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
        fixedDelayString = "\${fint.kontroll.publishing.fixed-delay:PT5M}",
        initialDelayString = "\${fint.kontroll.publishing.fixed-delay:PT5M}"
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
