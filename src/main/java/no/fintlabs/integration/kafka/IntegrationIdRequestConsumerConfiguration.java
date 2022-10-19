package no.fintlabs.integration.kafka;

import no.fintlabs.integration.IntegrationRepository;
import no.fintlabs.integration.model.entities.Integration;
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
public class IntegrationIdRequestConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, SourceApplicationIdAndSourceApplicationIntegrationIdDto> integrationIdRequestConsumer(
            RequestConsumerFactoryService requestConsumerFactoryService,
            RequestTopicService requestTopicService,
            IntegrationRepository integrationRepository
    ) {
        RequestTopicNameParameters requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .resource("integration-id")
                .parameterName("source-application-id-and-source-application-integration-id")
                .build();
        requestTopicService
                .ensureTopic(requestTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        return requestConsumerFactoryService.createFactory(
                SourceApplicationIdAndSourceApplicationIntegrationIdDto.class,
                String.class,
                (ConsumerRecord<String, SourceApplicationIdAndSourceApplicationIntegrationIdDto> consumerRecord) -> {

                    String integrationId = integrationRepository
                            .findIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
                                    consumerRecord.value().getSourceApplicationId(),
                                    consumerRecord.value().getSourceApplicationIntegrationId()
                            )
                            .map(Integration::getId)
                            .map(String::valueOf)
                            .orElse(null);

                    return ReplyProducerRecord
                            .<String>builder()
                            .value(integrationId)
                            .build();
                },
                new CommonLoggingErrorHandler()
        ).createContainer(requestTopicNameParameters);
    }

}
