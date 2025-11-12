package no.novari.fintkontrolldevicefactory.kafka

import no.fintlabs.kafka.topic.configuration.EntityCleanupFrequency
import no.fintlabs.kafka.topic.configuration.EntityTopicConfiguration
import no.fintlabs.kafka.topic.name.EntityTopicNameParameters
import no.fintlabs.kafka.topic.name.TopicNamePrefixParameters
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.DefaultErrorHandler
import java.time.Duration

object KafkaUtils {

    private fun topicParams() = TopicNamePrefixParameters.builder()
        .orgIdApplicationDefault()
        .domainContextApplicationDefault()
        .build()

    fun entityTopicConfiguration() = EntityTopicConfiguration.builder()
        .partitions(1)
        .lastValueRetainedForever()
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