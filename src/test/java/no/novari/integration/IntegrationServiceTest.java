package no.novari.integration;

import no.novari.integration.model.dtos.IntegrationDto;
import no.novari.integration.model.dtos.IntegrationPatchDto;
import no.novari.integration.model.dtos.IntegrationPostDto;
import no.novari.integration.model.entities.Integration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IntegrationServiceTest {

    @Mock
    private IntegrationRepository integrationRepository;

    @Mock
    private IntegrationMappingService integrationMappingService;

    @InjectMocks
    private IntegrationService integrationService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindAll() {
        List<Integration> integrations = new ArrayList<>();
        List<IntegrationDto> integrationDtos = new ArrayList<>();
        when(integrationRepository.findAll()).thenReturn(integrations);
        when(integrationMappingService.toDtos(integrations)).thenReturn(integrationDtos);

        var result = integrationService.findAll();

        assertEquals(integrationDtos, result);
    }

    @Test
    public void testFindAllWithPageable() {
        Page<Integration> integrations = new PageImpl<>(new ArrayList<>());
        Page<IntegrationDto> integrationDtos = new PageImpl<>(new ArrayList<>());
        when(integrationRepository.findAll(any(Pageable.class))).thenReturn(integrations);
        when(integrationMappingService.toDto(any(Integration.class))).thenAnswer(i -> integrationDtos.getContent().get(0));

        var result = integrationService.findAll(Pageable.unpaged());

        assertEquals(integrationDtos, result);
    }

    @Test
    public void testExistsById() {
        when(integrationRepository.existsById(anyLong())).thenReturn(true);

        boolean result = integrationService.existsById(1L);

        assertTrue(result);
    }

    @Test
    public void testFindById() {
        Integration integration = new Integration();
        IntegrationDto integrationDto = IntegrationDto.builder().build();

        when(integrationRepository.findById(anyLong())).thenReturn(Optional.of(integration));
        when(integrationMappingService.toDto(integration)).thenReturn(integrationDto);

        Optional<IntegrationDto> result = integrationService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(integrationDto, result.get());
    }

    @Test
    public void testFindIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId() {
        Integration integration = new Integration();
        IntegrationDto integrationDto = IntegrationDto.builder().build();

        when(integrationRepository.findIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(anyLong(), anyString()))
                .thenReturn(Optional.of(integration));
        when(integrationMappingService.toDto(integration)).thenReturn(integrationDto);

        Optional<IntegrationDto> result = integrationService.findIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(1L, "testId");

        assertTrue(result.isPresent());
        assertEquals(integrationDto, result.get());
    }

    @Test
    public void testExistsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId() {
        when(integrationRepository.existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(anyLong(), anyString()))
                .thenReturn(true);

        boolean result = integrationService.existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(1L, "testId");

        assertTrue(result);
    }

    @Test
    public void testFindActiveConfigurationIdByIntegrationId() {
        Integration integration = Integration.builder().activeConfigurationId(1L).build();

        when(integrationRepository.findById(anyLong())).thenReturn(Optional.of(integration));

        Optional<Long> result = integrationService.findActiveConfigurationIdByIntegrationId(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get());
    }

    @Test
    public void testSave() {
        IntegrationPostDto integrationPostDto = IntegrationPostDto.builder().build();
        Integration integration = new Integration();
        IntegrationDto integrationDto = IntegrationDto.builder().build();

        when(integrationMappingService.toIntegration(integrationPostDto)).thenReturn(integration);
        when(integrationRepository.save(integration)).thenReturn(integration);
        when(integrationMappingService.toDto(integration)).thenReturn(integrationDto);

        IntegrationDto result = integrationService.save(integrationPostDto);

        assertEquals(integrationDto, result);
    }

    @Test
    public void testUpdateById() {
        Long id = 1L;
        IntegrationPatchDto integrationPatchDto = mock(IntegrationPatchDto.class);
        Integration integration = new Integration();
        IntegrationDto integrationDto = IntegrationDto.builder().build();

        when(integrationRepository.getReferenceById(id)).thenReturn(integration);
        when(integrationRepository.save(integration)).thenReturn(integration);
        when(integrationMappingService.toDto(integration)).thenReturn(integrationDto);

        IntegrationDto result = integrationService.updateById(id, integrationPatchDto);

        assertEquals(integrationDto, result);
    }
}
