package no.novari.flyt.integration;

import no.novari.flyt.integration.model.dtos.IntegrationDto;
import no.novari.flyt.integration.model.dtos.IntegrationPostDto;
import no.novari.flyt.integration.model.entities.Integration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationMappingServiceTest {
    private IntegrationMappingService integrationMappingService;

    @BeforeEach
    public void setUp() {
        integrationMappingService = new IntegrationMappingService();
    }

    @Test
    public void testToIntegration() {
        IntegrationPostDto integrationPostDto = IntegrationPostDto
                .builder()
                .sourceApplicationId(1L)
                .sourceApplicationIntegrationId("Test")
                .build();

        Integration integration = integrationMappingService.toIntegration(integrationPostDto);

        assertNotNull(integration);
        assertEquals(integrationPostDto.getSourceApplicationId(), integration.getSourceApplicationId());
        assertEquals(integrationPostDto.getSourceApplicationIntegrationId(), integration.getSourceApplicationIntegrationId());
        assertEquals(integrationPostDto.getDestination(), integration.getDestination());
        assertEquals(Integration.State.DEACTIVATED, integration.getState());
    }

    @Test
    public void testToDto() {
        Integration integration = Integration.builder()
                .id(1L)
                .sourceApplicationId(1L)
                .sourceApplicationIntegrationId("Test")
                .destination("Destination")
                .state(Integration.State.ACTIVE)
                .activeConfigurationId(2L)
                .build();

        IntegrationDto integrationDto = integrationMappingService.toDto(integration);

        assertNotNull(integrationDto);
        assertEquals(integration.getId(), integrationDto.getId());
        assertEquals(integration.getSourceApplicationId(), integrationDto.getSourceApplicationId());
        assertEquals(integration.getSourceApplicationIntegrationId(), integrationDto.getSourceApplicationIntegrationId());
        assertEquals(integration.getDestination(), integrationDto.getDestination());
        assertEquals(integration.getState(), integrationDto.getState());
        assertEquals(integration.getActiveConfigurationId(), integrationDto.getActiveConfigurationId());
    }

    @Test
    public void testToDtos() {
        Integration integration1 = Integration.builder()
                .id(1L)
                .sourceApplicationId(1L)
                .sourceApplicationIntegrationId("Test1")
                .destination("Destination1")
                .state(Integration.State.ACTIVE)
                .activeConfigurationId(2L)
                .build();

        Integration integration2 = Integration.builder()
                .id(2L)
                .sourceApplicationId(2L)
                .sourceApplicationIntegrationId("Test2")
                .destination("Destination2")
                .state(Integration.State.DEACTIVATED)
                .activeConfigurationId(3L)
                .build();

        List<Integration> integrations = Arrays.asList(integration1, integration2);
        Collection<IntegrationDto> integrationDtos = integrationMappingService.toDtos(integrations);

        assertNotNull(integrationDtos);
        assertEquals(integrations.size(), integrationDtos.size());
    }
}
