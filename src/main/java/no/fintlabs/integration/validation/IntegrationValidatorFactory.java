package no.fintlabs.integration.validation;

import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import no.fintlabs.integration.kafka.ConfigurationRequestProducerService;
import org.hibernate.validator.HibernateValidatorFactory;
import org.springframework.stereotype.Service;

@Service
public class IntegrationValidatorFactory {

    private final ValidatorFactory validatorFactory;

    private final ConfigurationRequestProducerService configurationRequestProducerService;

    public IntegrationValidatorFactory(
            ValidatorFactory validatorFactory,
            ConfigurationRequestProducerService configurationRequestProducerService
    ) {
        this.validatorFactory = validatorFactory;
        this.configurationRequestProducerService = configurationRequestProducerService;
    }

    public Validator getValidator() {
        return validatorFactory.getValidator();
    }

    public Validator getPatchValidator(Long integrationId, Long configurationId) {
        return validatorFactory
                .unwrap(HibernateValidatorFactory.class)
                .usingContext()
                .constraintValidatorPayload(getConfigurationValidationContext(integrationId, configurationId))
                .getValidator();
    }

    private IntegrationValidationContext getConfigurationValidationContext(Long integrationId, Long configurationId) {
        return IntegrationValidationContext
                .builder()
                .integrationId(integrationId)
                .configuration(configurationId == null
                        ? null
                        : configurationRequestProducerService.get(configurationId).orElse(null))
                .build();

    }

}
