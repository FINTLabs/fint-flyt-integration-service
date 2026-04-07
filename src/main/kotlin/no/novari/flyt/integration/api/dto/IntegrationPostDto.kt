package no.novari.flyt.integration.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class IntegrationPostDto(
    @field:NotNull
    val sourceApplicationId: Long? = null,
    @field:NotBlank
    val sourceApplicationIntegrationId: String? = null,
    @field:NotBlank
    val destination: String? = null,
)
