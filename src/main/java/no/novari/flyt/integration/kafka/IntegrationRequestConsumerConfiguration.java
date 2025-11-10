package no.novari.flyt.integration.kafka;

import no.novari.flyt.integration.IntegrationService;
import no.novari.flyt.integration.model.dtos.IntegrationDto;
import no.novari.flyt.integration.model.dtos.SourceApplicationIdAndSourceApplicationIntegrationIdDto;
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
public class IntegrationRequestConsumerConfiguration {

    private static final Duration RETENTION_TIME = Duration.ofMinutes(5);

    @Bean
    public ConcurrentMessageListenerContainer<String, Long>
    integrationByIntegrationIdRequestConsumer(
            RequestListenerContainerFactory requestListenerContainerFactory,
            RequestTopicService requestTopicService,
            IntegrationService integrationService,
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
                .resourceName("integration")
                .parameterName("integration-id")
                .build();
        requestTopicService
                .createOrModifyTopic(requestTopicNameParameters, RequestTopicConfiguration.builder().retentionTime(RETENTION_TIME).build());

        return requestListenerContainerFactory.createRecordConsumerFactory(
                Long.class,
                IntegrationDto.class,
                (ConsumerRecord<String, Long> consumerRecord) -> ReplyProducerRecord
                        .<IntegrationDto>builder()
                        .value(integrationService
                                .findById(consumerRecord.value())
                                .orElse(null))
                        .build()
                ,
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

    @Bean
    public ConcurrentMessageListenerContainer<String, SourceApplicationIdAndSourceApplicationIntegrationIdDto>
    integrationBySourceApplicationIdAndSourceApplicationIntegrationIdRequestConsumer(
            RequestListenerContainerFactory requestListenerContainerFactory,
            RequestTopicService requestTopicService,
            IntegrationService integrationService,
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
                .resourceName("integration")
                .parameterName("source-application-id-and-source-application-integration-id")
                .build();
        requestTopicService
                .createOrModifyTopic(requestTopicNameParameters, RequestTopicConfiguration
                        .builder()
                        .retentionTime(RETENTION_TIME)
                        .build()
                );

        return requestListenerContainerFactory.createRecordConsumerFactory(
                SourceApplicationIdAndSourceApplicationIntegrationIdDto.class,
                IntegrationDto.class,
                (ConsumerRecord<String, SourceApplicationIdAndSourceApplicationIntegrationIdDto> consumerRecord) -> {

                    IntegrationDto integrationDto = integrationService
                            .findIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
                                    consumerRecord.value().getSourceApplicationId(),
                                    consumerRecord.value().getSourceApplicationIntegrationId()
                            )
                            .orElse(null);

                    return ReplyProducerRecord
                            .<IntegrationDto>builder()
                            .value(integrationDto)
                            .build();
                },
                RequestListenerConfiguration
                        .stepBuilder(SourceApplicationIdAndSourceApplicationIntegrationIdDto.class)
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
