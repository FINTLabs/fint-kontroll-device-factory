package no.novari.fintkontrolldevicefactory.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import no.fintlabs.cache.FintCache
import no.fintlabs.kafka.model.ParameterizedProducerRecord
import no.fintlabs.kafka.producing.ParameterizedTemplate
import no.fintlabs.kafka.producing.ParameterizedTemplateFactory
import no.fintlabs.kafka.topic.EntityTopicService
import no.fintlabs.kafka.topic.name.EntityTopicNameParameters
import no.novari.fintkontrolldevicefactory.entity.DeviceGroupMembership
import no.novari.fintkontrolldevicefactory.service.MembershipService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class MembershipPublishingComponent(
    private val membershipService: MembershipService,
    parameterizedTemplateFactory: ParameterizedTemplateFactory,
    entityTopicService: EntityTopicService,
    private val kontrollDeviceGroupMembershipCache: FintCache<String, DeviceGroupMembership>
) {

    private val template: ParameterizedTemplate<DeviceGroupMembership> =
        parameterizedTemplateFactory.createTemplate(DeviceGroupMembership::class.java)

    private val nameParams: EntityTopicNameParameters =
        KafkaUtils.entityTopicNameParameters("device-group-membership")

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
        val all = membershipService.getAllMemberships()

        val toPublish = all
            .filter { membership -> !kontrollDeviceGroupMembershipCache.containsKey(membership.getId()) }


        toPublish.forEach { sendOne(it) }

        logger.info { "Found ${all.size} memberships, published ${toPublish.size} out of them." }
    }

    private fun sendOne(membership: DeviceGroupMembership) {
        val record = ParameterizedProducerRecord.builder<DeviceGroupMembership>()
            .topicNameParameters(nameParams)
            .key(membership.getId())
            .value(membership)
            .build()

        template.send(record)
        logger.info { "Published membership ${membership.getId()}" }
    }
}
