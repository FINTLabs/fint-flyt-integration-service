package no.novari.flyt.integration.validation

import jakarta.validation.Payload
import no.novari.flyt.integration.api.dto.ConfigurationDto

data class IntegrationValidationContext(
    val integrationId: Long? = null,
    val configuration: ConfigurationDto? = null,
) : Payload
