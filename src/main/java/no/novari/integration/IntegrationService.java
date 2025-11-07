package no.novari.integration;

import no.novari.integration.model.dtos.IntegrationDto;
import no.novari.integration.model.dtos.IntegrationPatchDto;
import no.novari.integration.model.dtos.IntegrationPostDto;
import no.novari.integration.model.entities.Integration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

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

    public Page<IntegrationDto> findAll(Pageable pageable) {
        return integrationRepository.findAll(pageable)
                .map(integrationMappingService::toDto);
    }

    public Collection<IntegrationDto> findAllBySourceApplicationIds(Set<Long> sourceApplicationIds) {
        return integrationMappingService.toDtos(
                integrationRepository.findIntegrationsBySourceApplicationIdIn(sourceApplicationIds)
        );
    }

    public Page<IntegrationDto> findAllBySourceApplicationIds(
            Set<Long> sourceApplicationIds,
            Pageable pageable
    ) {
        return integrationRepository.findIntegrationsBySourceApplicationIdIn(sourceApplicationIds, pageable)
                .map(integrationMappingService::toDto);
    }

    public boolean existsById(Long integrationId) {
        return integrationRepository.existsById(integrationId);
    }

    public Optional<IntegrationDto> findById(Long integrationId) {
        return integrationRepository.findById(integrationId)
                .map(integrationMappingService::toDto);
    }

    public Optional<IntegrationDto> findIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
            Long sourceApplicationId, String sourceApplicationIntegrationId
    ) {
        return integrationRepository.findIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
                        sourceApplicationId, sourceApplicationIntegrationId
                )
                .map(integrationMappingService::toDto);
    }

    public boolean existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
            Long sourceApplicationId, String sourceApplicationIntegrationId
    ) {
        return integrationRepository.existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
                sourceApplicationId, sourceApplicationIntegrationId
        );
    }

    public Optional<Long> findActiveConfigurationIdByIntegrationId(Long integrationId) {
        return integrationRepository.findById(integrationId)
                .map(Integration::getActiveConfigurationId);
    }

    public IntegrationDto save(IntegrationPostDto integrationPostDto) {
        return integrationMappingService.toDto(
                integrationRepository.save(
                        integrationMappingService.toIntegration(integrationPostDto)
                )
        );
    }

    public IntegrationDto updateById(Long integrationId, IntegrationPatchDto integrationPatchDto) {
        Integration integration = integrationRepository.getReferenceById(integrationId);

        integrationPatchDto.getDestination().ifPresent(integration::setDestination);
        integrationPatchDto.getState().ifPresent(integration::setState);
        integrationPatchDto.getActiveConfigurationId().ifPresent(integration::setActiveConfigurationId);

        return integrationMappingService.toDto(
                integrationRepository.save(integration)
        );
    }

}
