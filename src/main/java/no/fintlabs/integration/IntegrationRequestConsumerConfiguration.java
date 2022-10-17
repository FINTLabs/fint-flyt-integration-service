package no.fintlabs.integration;

import no.fintlabs.integration.model.Integration;
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
    public ConcurrentMessageListenerContainer<String, Long> IntegrationRequestConsumerConfiguration(
            RequestConsumerFactoryService requestConsumerFactoryService,
            RequestTopicService requestTopicService,
            IntegrationRepository integrationRepository
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
                Integration.class,
                (ConsumerRecord<String, Long> consumerRecord) -> ReplyProducerRecord
                        .<Integration>builder()
                        .value(integrationRepository
                                .findById(consumerRecord.value())
                                .orElse(null))
                        .build(),
        new CommonLoggingErrorHandler()
        ).createContainer(requestTopicNameParameters);
    }
}