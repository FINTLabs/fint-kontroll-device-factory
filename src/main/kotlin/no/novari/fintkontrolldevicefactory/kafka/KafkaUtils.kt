package no.novari.fintkontrolldevicefactory.kafka

import no.novari.kafka.topic.configuration.EntityCleanupFrequency
import no.novari.kafka.topic.configuration.EntityTopicConfiguration
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.DefaultErrorHandler
import java.time.Duration

object KafkaUtils {

    private fun topicParams() = TopicNamePrefixParameters.stepBuilder()
        .orgIdApplicationDefault()
        .domainContextApplicationDefault()
        .build()

    fun entityTopicConfiguration() = EntityTopicConfiguration.stepBuilder()
        .partitions(1)
        .lastValueRetentionTime(Duration.ofDays(7))
        .nullValueRetentionTime(Duration.ZERO) // TODO check
        .cleanupFrequency(EntityCleanupFrequency.NORMAL)
        .build()

     fun entityTopicNameParameters(resourceName: String) =
        EntityTopicNameParameters.builder()
            .resourceName(resourceName)
            .topicNamePrefixParameters(topicParams())
            .build()
}


@Configuration
class KafkaConfig {

    @Bean
    fun kafkaCommonErrorHandler(): CommonErrorHandler = DefaultErrorHandler()
}