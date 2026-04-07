package no.novari.flyt.integration.application

import no.novari.flyt.integration.api.dto.IntegrationDto
import no.novari.flyt.integration.validation.IntegrationValidatorFactory
import no.novari.flyt.integration.validation.ValidationErrorsFormattingService
import no.novari.flyt.integration.web.UnprocessableEntityException
import org.springframework.stereotype.Service

@Service
class IntegrationUpdateValidationService(
    private val integrationValidatorFactory: IntegrationValidatorFactory,
    private val validationErrorsFormattingService: ValidationErrorsFormattingService,
) {
    fun validate(
        integrationId: Long,
        integration: IntegrationDto,
    ) {
        val constraintViolations =
            integrationValidatorFactory
                .getPatchValidator(integrationId, integration.activeConfigurationId)
                .validate(integration)

        if (constraintViolations.isNotEmpty()) {
            throw UnprocessableEntityException(validationErrorsFormattingService.format(constraintViolations))
        }
    }
}
