package no.fintlabs.integration.kafka;

import no.fintlabs.integration.IntegrationRepository;
import no.fintlabs.integration.model.entities.Integration;
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
public class ActiveConfigurationIdRequestConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, Long> activeConfigurationIdRequestConsumer(
            RequestConsumerFactoryService requestConsumerFactoryService,
            RequestTopicService requestTopicService,
            IntegrationRepository integrationRepository
    ) {
        RequestTopicNameParameters requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .resource("active-configuration-id")
                .parameterName("integration-id")
                .build();
        requestTopicService
                .ensureTopic(requestTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        return requestConsumerFactoryService.createFactory(
                Long.class,
                Long.class,
                (ConsumerRecord<String, Long> consumerRecord) -> {
                    Long integrationId = consumerRecord.value();

                    Long activeConfigurationId = integrationRepository
                            .findById(integrationId)
                            .map(Integration::getActiveConfigurationId)
                            .orElse(null);

                    return ReplyProducerRecord
                            .<Long>builder()
                            .value(activeConfigurationId)
                            .build();
                },
                new CommonLoggingErrorHandler()
        ).createContainer(requestTopicNameParameters);
    }

}
