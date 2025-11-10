package no.novari.flyt.integration.kafka;

import no.novari.flyt.integration.IntegrationService;
import no.fintlabs.kafka.consuming.ErrorHandlerConfiguration;
import no.fintlabs.kafka.consuming.ErrorHandlerFactory;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.kafka.requestreply.RequestListenerConfiguration;
import no.fintlabs.kafka.requestreply.RequestListenerContainerFactory;
import no.fintlabs.kafka.requestreply.topic.RequestTopicService;
import no.fintlabs.kafka.requestreply.topic.configuration.RequestTopicConfiguration;
import no.fintlabs.kafka.requestreply.topic.name.RequestTopicNameParameters;
import no.fintlabs.kafka.topic.name.TopicNamePrefixParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.time.Duration;

@Configuration
public class ActiveConfigurationIdRequestConsumerConfiguration {

    private static final Duration RETENTION_TIME = Duration.ofMinutes(5);

    @Bean
    public ConcurrentMessageListenerContainer<String, Long> activeConfigurationIdRequestConsumer(
            RequestTopicService requestTopicService,
            IntegrationService integrationService,
            RequestListenerContainerFactory requestListenerContainerFactory,
            ErrorHandlerFactory errorHandlerFactory
    ) {
        RequestTopicNameParameters requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .builder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .resourceName("active-configuration-id")
                .parameterName("integration-id")
                .build();
        requestTopicService
                .createOrModifyTopic(requestTopicNameParameters, RequestTopicConfiguration
                        .builder()
                        .retentionTime(RETENTION_TIME)
                        .build()
                );

        return requestListenerContainerFactory.createRecordConsumerFactory(
                Long.class,
                Long.class,
                (ConsumerRecord<String, Long> consumerRecord) -> {
                    Long integrationId = consumerRecord.value();

                    Long activeConfigurationId = integrationService
                            .findActiveConfigurationIdByIntegrationId(integrationId)
                            .orElse(null);

                    return ReplyProducerRecord
                            .<Long>builder()
                            .value(activeConfigurationId)
                            .build();
                },
                RequestListenerConfiguration
                        .stepBuilder(Long.class)
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .build(),
                errorHandlerFactory.createErrorHandler(
                        ErrorHandlerConfiguration
                                .stepBuilder()
                                .noRetries()
                                .skipFailedRecords()
                                .build()
                )
        ).createContainer(requestTopicNameParameters);
    }

}
