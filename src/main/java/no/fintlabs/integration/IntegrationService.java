package no.fintlabs.integration;

import no.fintlabs.integration.model.entities.Integration;
import no.fintlabs.integration.model.dtos.IntegrationDto;
import no.fintlabs.integration.model.dtos.IntegrationPatchDto;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class IntegrationService {

    private final IntegrationRepository integrationRepository;
    private final IntegrationMappingService integrationMappingService;


    public IntegrationService(
            IntegrationRepository integrationRepository,
            IntegrationMappingService integrationMappingService
    ) {
        this.integrationRepository = integrationRepository;
        this.integrationMappingService = integrationMappingService;
    }

    public Collection<IntegrationDto> findAll() {
        return integrationMappingService.toDtos(
                integrationRepository.findAll()
        );
    }

    public Optional<IntegrationDto> findById(Long integrationId) {
        return integrationRepository.findById(integrationId)
                .map(integrationMappingService::toDto);
    }

    public boolean existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
            Long sourceApplicationId, String sourceApplicationIntegrationId
    ) {
        return integrationRepository.existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
                sourceApplicationId, sourceApplicationIntegrationId
        );
    }

    public IntegrationDto save(IntegrationDto integrationDto) {
        return integrationMappingService.toDto(
                integrationRepository.save(
                        integrationMappingService.toIntegration(integrationDto)
                )
        );
    }

    public IntegrationDto updateById(Long integrationId, IntegrationPatchDto integrationPatchDto) {
        Integration integration = integrationRepository.getById(integrationId);

        integrationPatchDto.getDestination().ifPresent(integration::setDestination);
        integrationPatchDto.getState().ifPresent(integration::setState);
        integrationPatchDto.getActiveConfigurationId().ifPresent(integration::setActiveConfigurationId);

        return integrationMappingService.toDto(
                integrationRepository.save(integration)
        );
    }

}
