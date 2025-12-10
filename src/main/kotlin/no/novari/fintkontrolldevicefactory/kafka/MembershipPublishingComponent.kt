package no.novari.fintkontrolldevicefactory.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import no.novari.cache.FintCache
import no.novari.kafka.producing.ParameterizedTemplate
import no.novari.kafka.producing.ParameterizedTemplateFactory
import no.novari.kafka.topic.EntityTopicService
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.fintkontrolldevicefactory.entity.DeviceGroupMembership
import no.novari.fintkontrolldevicefactory.service.MembershipService
import no.novari.kafka.producing.ParameterizedProducerRecord
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
        fixedDelayString = "\${novari.kontroll.publishing.fixed-delay:PT5M}",
        initialDelayString = "\${novari.kontroll.publishing.membership-initial-delay:PT7M}"
    )
    fun publishAll() {
        val all = membershipService.getAllMemberships()

        val toPublish = all
            .filter { membership -> !kontrollDeviceGroupMembershipCache.containsKey(membership.systemId) }


        toPublish.forEach { sendOne(it) }

        logger.info { "Found ${all.size} memberships, published ${toPublish.size} out of them." }
    }

    private fun sendOne(membership: DeviceGroupMembership) {
        val record = ParameterizedProducerRecord.builder<DeviceGroupMembership>()
            .topicNameParameters(nameParams)
            .key(membership.systemId)
            .value(membership)
            .build()

        template.send(record)
        logger.info { "Published membership ${membership.systemId}" }
    }
}
