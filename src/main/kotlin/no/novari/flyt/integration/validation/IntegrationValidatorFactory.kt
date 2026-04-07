package no.novari.flyt.integration.validation

import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import no.novari.flyt.integration.kafka.ConfigurationRequestProducerService
import org.hibernate.validator.HibernateValidatorFactory
import org.springframework.stereotype.Service

@Service
class IntegrationValidatorFactory(
    private val validatorFactory: ValidatorFactory,
    private val configurationRequestProducerService: ConfigurationRequestProducerService,
) {
    fun getPatchValidator(
        integrationId: Long,
        configurationId: Long?,
    ): Validator {
        return validatorFactory
            .unwrap(HibernateValidatorFactory::class.java)
            .usingContext()
            .constraintValidatorPayload(getConfigurationValidationContext(integrationId, configurationId))
            .validator
    }

    private fun getConfigurationValidationContext(
        integrationId: Long,
        configurationId: Long?,
    ): IntegrationValidationContext {
        return IntegrationValidationContext(
            integrationId = integrationId,
            configuration = configurationId?.let(configurationRequestProducerService::get),
        )
    }
}
