package no.fintlabs.integration;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.integration.model.Integration;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.kafka.requestreply.RequestConsumerFactoryService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Slf4j
@Configuration
public class ActiveConfigurationIdRequestConsumerConfiguration {

    @Bean
    public ConcurrentMessageListenerContainer<String, String> activeConfigurationIdRequestConsumer(
            RequestConsumerFactoryService requestConsumerFactoryService,
            IntegrationRepository integrationRepository
    ) {
        return requestConsumerFactoryService.createFactory(
                String.class,
                String.class,
                (ConsumerRecord<String, String> consumerRecord) -> {
                    String integrationId = consumerRecord.value();

                    String activeConfigurationId = integrationRepository
                            .findById(Long.parseLong(integrationId))
                            .map(Integration::getActiveConfigurationId)
                            .orElse(null);

                    return ReplyProducerRecord
                            .<String>builder()
                            .value(activeConfigurationId)
                            .build();
                },
                new CommonLoggingErrorHandler()
        ).createContainer(
                RequestTopicNameParameters
                        .builder()
                        .resource("active-configuration-id")
                        .parameterName("integration-id")
                        .build()
        );
    }

}
