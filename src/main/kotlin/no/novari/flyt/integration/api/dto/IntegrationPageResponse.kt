package no.novari.flyt.integration.api.dto

data class IntegrationPageResponse(
    val content: List<IntegrationDto>,
    val totalElements: Long,
    val totalPages: Int,
)
