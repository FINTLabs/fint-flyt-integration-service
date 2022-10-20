package no.fintlabs.integration.kafka;

import no.fintlabs.integration.IntegrationService;
import no.fintlabs.integration.model.dtos.IntegrationDto;
import no.fintlabs.integration.model.dtos.SourceApplicationIdAndSourceApplicationIntegrationIdDto;
import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.kafka.requestreply.RequestConsumerFactoryService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class IntegrationRequestConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, Long>
    integrationByIntegrationIdRequestConsumer(
            RequestConsumerFactoryService requestConsumerFactoryService,
            RequestTopicService requestTopicService,
            IntegrationService integrationService
    ) {
        RequestTopicNameParameters requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .resource("integration")
                .parameterName("integration-id")
                .build();
        requestTopicService
                .ensureTopic(requestTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        return requestConsumerFactoryService.createFactory(
                Long.class,
                IntegrationDto.class,
                (ConsumerRecord<String, Long> consumerRecord) -> ReplyProducerRecord
                        .<IntegrationDto>builder()
                        .value(integrationService
                                .findById(consumerRecord.value())
                                .orElse(null))
                        .build(),
                new CommonLoggingErrorHandler()
        ).createContainer(requestTopicNameParameters);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, SourceApplicationIdAndSourceApplicationIntegrationIdDto>
    integrationBySourceApplicationIdAndSourceApplicationIntegrationIdRequestConsumer(
            RequestConsumerFactoryService requestConsumerFactoryService,
            RequestTopicService requestTopicService,
            IntegrationService integrationService
    ) {
        RequestTopicNameParameters requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .resource("integration")
                .parameterName("source-application-id-and-source-application-integration-id")
                .build();
        requestTopicService
                .ensureTopic(requestTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        return requestConsumerFactoryService.createFactory(
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
                new CommonLoggingErrorHandler()
        ).createContainer(requestTopicNameParameters);
    }

}
