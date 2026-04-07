package no.novari.flyt.integration.api.dto

import jakarta.validation.GroupSequence
import no.novari.flyt.integration.persistence.entity.Integration
import no.novari.flyt.integration.validation.constraints.ActiveConfigurationIsDefinedIfStateIsActive
import no.novari.flyt.integration.validation.constraints.ReferencedConfigurationExists
import no.novari.flyt.integration.validation.constraints.ReferencedConfigurationIsComplete
import no.novari.flyt.integration.validation.constraints.ReferencedConfigurationIsForIntegration
import no.novari.flyt.integration.validation.groups.ActiveConfigurationIsCompleteGroup
import no.novari.flyt.integration.validation.groups.ActiveConfigurationIsForIntegrationGroup

@GroupSequence(
    IntegrationDto::class,
    ActiveConfigurationIsForIntegrationGroup::class,
    ActiveConfigurationIsCompleteGroup::class,
)
data class IntegrationDto(
    val id: Long,
    val sourceApplicationId: Long,
    val sourceApplicationIntegrationId: String,
    val destination: String,
    @field:ActiveConfigurationIsDefinedIfStateIsActive
    val state: Integration.State,
    @field:ReferencedConfigurationExists
    @field:ReferencedConfigurationIsForIntegration(groups = [ActiveConfigurationIsForIntegrationGroup::class])
    @field:ReferencedConfigurationIsComplete(groups = [ActiveConfigurationIsCompleteGroup::class])
    val activeConfigurationId: Long? = null,
)
