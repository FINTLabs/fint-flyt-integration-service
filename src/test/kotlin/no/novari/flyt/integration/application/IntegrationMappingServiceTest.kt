package no.novari.flyt.integration.application

import no.novari.flyt.integration.api.dto.IntegrationPostDto
import no.novari.flyt.integration.persistence.entity.Integration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IntegrationMappingServiceTest {
    private lateinit var integrationMappingService: IntegrationMappingService

    @BeforeEach
    fun setUp() {
        integrationMappingService = IntegrationMappingService()
    }

    @Test
    fun shouldMapPostDtoToIntegration() {
        val integrationPostDto =
            IntegrationPostDto(
                sourceApplicationId = 1L,
                sourceApplicationIntegrationId = "Test",
                destination = "Destination",
            )

        val integration = integrationMappingService.toIntegration(integrationPostDto)

        assertNotNull(integration)
        assertEquals(integrationPostDto.sourceApplicationId, integration.sourceApplicationId)
        assertEquals(
            integrationPostDto.sourceApplicationIntegrationId,
            integration.sourceApplicationIntegrationId,
        )
        assertEquals(integrationPostDto.destination, integration.destination)
        assertEquals(Integration.State.DEACTIVATED, integration.state)
    }

    @Test
    fun shouldMapIntegrationToDto() {
        val integration =
            Integration(
                id = 1L,
                sourceApplicationId = 1L,
                sourceApplicationIntegrationId = "Test",
                destination = "Destination",
                state = Integration.State.ACTIVE,
                activeConfigurationId = 2L,
            )

        val integrationDto = integrationMappingService.toDto(integration)

        assertNotNull(integrationDto)
        assertEquals(integration.id, integrationDto.id)
        assertEquals(integration.sourceApplicationId, integrationDto.sourceApplicationId)
        assertEquals(
            integration.sourceApplicationIntegrationId,
            integrationDto.sourceApplicationIntegrationId,
        )
        assertEquals(integration.destination, integrationDto.destination)
        assertEquals(integration.state, integrationDto.state)
        assertEquals(integration.activeConfigurationId, integrationDto.activeConfigurationId)
    }

    @Test
    fun shouldMapIntegrationCollectionToDtos() {
        val integrations =
            listOf(
                Integration(
                    id = 1L,
                    sourceApplicationId = 1L,
                    sourceApplicationIntegrationId = "Test1",
                    destination = "Destination1",
                    state = Integration.State.ACTIVE,
                    activeConfigurationId = 2L,
                ),
                Integration(
                    id = 2L,
                    sourceApplicationId = 2L,
                    sourceApplicationIntegrationId = "Test2",
                    destination = "Destination2",
                    state = Integration.State.DEACTIVATED,
                    activeConfigurationId = 3L,
                ),
            )

        val integrationDtos = integrationMappingService.toDtos(integrations)

        assertNotNull(integrationDtos)
        assertEquals(integrations.size, integrationDtos.size)
    }
}
