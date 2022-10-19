package no.fintlabs.integration;

import no.fintlabs.integration.model.dtos.IntegrationDto;
import no.fintlabs.integration.model.dtos.IntegrationPostDto;
import no.fintlabs.integration.model.entities.Integration;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class IntegrationMappingService {

    public Integration toIntegration(IntegrationPostDto integrationPostDto) {
        return Integration
                .builder()
                .sourceApplicationId(integrationPostDto.getSourceApplicationId())
                .sourceApplicationIntegrationId(integrationPostDto.getSourceApplicationIntegrationId())
                .destination(integrationPostDto.getDestination())
                .state(Integration.State.DEACTIVATED)
                .build();
    }

    public Collection<IntegrationDto> toDtos(Collection<Integration> integrations) {
        return integrations.stream().map(this::toDto).toList();
    }

    public IntegrationDto toDto(Integration integration) {
        return IntegrationDto
                .builder()
                .id(integration.getId())
                .sourceApplicationId(integration.getSourceApplicationId())
                .sourceApplicationIntegrationId(integration.getSourceApplicationIntegrationId())
                .destination(integration.getDestination())
                .state(integration.getState())
                .activeConfigurationId(integration.getActiveConfigurationId())
                .build();
    }

}
