package no.novari.flyt.integration.application

import no.novari.flyt.integration.api.dto.IntegrationDto
import no.novari.flyt.integration.api.dto.IntegrationPostDto
import no.novari.flyt.integration.persistence.entity.Integration
import org.springframework.stereotype.Service

@Service
class IntegrationMappingService {
    fun toIntegration(integrationPostDto: IntegrationPostDto): Integration {
        return Integration(
            sourceApplicationId = requireNotNull(integrationPostDto.sourceApplicationId),
            sourceApplicationIntegrationId = requireNotNull(integrationPostDto.sourceApplicationIntegrationId),
            destination = requireNotNull(integrationPostDto.destination),
            state = Integration.State.DEACTIVATED,
        )
    }

    fun toDtos(integrations: Collection<Integration>): List<IntegrationDto> {
        return integrations.map(this::toDto)
    }

    fun toDto(integration: Integration): IntegrationDto {
        return IntegrationDto(
            id = requireNotNull(integration.id),
            sourceApplicationId = requireNotNull(integration.sourceApplicationId),
            sourceApplicationIntegrationId = requireNotNull(integration.sourceApplicationIntegrationId),
            destination = requireNotNull(integration.destination),
            state = requireNotNull(integration.state),
            activeConfigurationId = integration.activeConfigurationId,
        )
    }
}
