package no.novari.flyt.integration.api.dto

data class ConfigurationDto(
    val id: Long? = null,
    val integrationId: Long? = null,
    val integrationMetadataId: Long? = null,
    val completed: Boolean = false,
    val comment: String? = null,
    val version: Int? = null,
)
