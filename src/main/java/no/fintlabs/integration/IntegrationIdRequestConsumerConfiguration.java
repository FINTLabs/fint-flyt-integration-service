package no.fintlabs.integration;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.integration.model.Integration;
import no.fintlabs.integration.model.SourceApplicationIdAndSourceApplicationIntegrationIdWrapper;
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

@Slf4j
@Configuration
public class IntegrationIdRequestConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, SourceApplicationIdAndSourceApplicationIntegrationIdWrapper> integrationIdRequestConsumer(
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
                SourceApplicationIdAndSourceApplicationIntegrationIdWrapper.class,
                String.class,
                (ConsumerRecord<String, SourceApplicationIdAndSourceApplicationIntegrationIdWrapper> consumerRecord) -> {

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
