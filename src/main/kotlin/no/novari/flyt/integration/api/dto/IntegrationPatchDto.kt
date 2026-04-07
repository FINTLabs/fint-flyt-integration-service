package no.novari.flyt.integration.api.dto

import no.novari.flyt.integration.persistence.entity.Integration

data class IntegrationPatchDto(
    val destination: String? = null,
    val state: Integration.State? = null,
    val activeConfigurationId: Long? = null,
)
