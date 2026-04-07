package no.novari.flyt.integration.application

import no.novari.flyt.integration.api.dto.IntegrationPatchDto
import no.novari.flyt.integration.api.dto.IntegrationPostDto
import no.novari.flyt.integration.persistence.IntegrationRepository
import no.novari.flyt.integration.persistence.entity.Integration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class IntegrationServiceTest {
    @Mock
    private lateinit var integrationRepository: IntegrationRepository

    private lateinit var integrationService: IntegrationService

    @BeforeEach
    fun setUp() {
        integrationService = IntegrationService(integrationRepository, IntegrationMappingService())
    }

    @Test
    fun shouldFindAllIntegrations() {
        val integrations =
            listOf(
                Integration(
                    id = 1L,
                    sourceApplicationId = 1L,
                    sourceApplicationIntegrationId = "integration-1",
                    destination = "destination-1",
                    state = Integration.State.ACTIVE,
                ),
            )
        whenever(integrationRepository.findAll()).thenReturn(integrations)

        val result = integrationService.findAll()

        assertEquals(1, result.size)
        assertEquals(1L, result.first().id)
    }

    @Test
    fun shouldFindAllWithPageable() {
        val integrations =
            PageImpl(
                listOf(
                    Integration(
                        id = 1L,
                        sourceApplicationId = 1L,
                        sourceApplicationIntegrationId = "integration-1",
                        destination = "destination-1",
                        state = Integration.State.ACTIVE,
                    ),
                ),
            )
        whenever(integrationRepository.findAll(any<Pageable>())).thenReturn(integrations)

        val result = integrationService.findAll(Pageable.unpaged())

        assertEquals(1, result.totalElements)
        assertEquals(1L, result.content.single().id)
    }

    @Test
    fun shouldCheckIfIntegrationExistsById() {
        whenever(integrationRepository.existsById(1L)).thenReturn(true)

        val result = integrationService.existsById(1L)

        assertTrue(result)
    }

    @Test
    fun shouldFindIntegrationById() {
        val integration =
            Integration(
                id = 1L,
                sourceApplicationId = 1L,
                sourceApplicationIntegrationId = "integration-1",
                destination = "destination-1",
                state = Integration.State.ACTIVE,
            )
        whenever(integrationRepository.findById(1L)).thenReturn(Optional.of(integration))

        val result = integrationService.findById(1L)

        assertNotNull(result)
        assertEquals(1L, result?.id)
    }

    @Test
    fun shouldReturnNullWhenIntegrationByIdIsMissing() {
        whenever(integrationRepository.findById(1L)).thenReturn(Optional.empty())

        val result = integrationService.findById(1L)

        assertNull(result)
    }

    @Test
    fun shouldFindIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId() {
        val integration =
            Integration(
                id = 1L,
                sourceApplicationId = 1L,
                sourceApplicationIntegrationId = "integration-1",
                destination = "destination-1",
                state = Integration.State.ACTIVE,
            )
        whenever(
            integrationRepository.findIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
                1L,
                "integration-1",
            ),
        ).thenReturn(integration)

        val result =
            integrationService.findIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
                1L,
                "integration-1",
            )

        assertNotNull(result)
        assertEquals(1L, result?.id)
    }

    @Test
    fun shouldCheckIfIntegrationExistsBySourceApplicationAndIntegrationId() {
        whenever(
            integrationRepository.existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
                1L,
                "integration-1",
            ),
        ).thenReturn(true)

        val result =
            integrationService.existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
                1L,
                "integration-1",
            )

        assertTrue(result)
    }

    @Test
    fun shouldFindActiveConfigurationIdByIntegrationId() {
        val integration = Integration(activeConfigurationId = 1L)
        whenever(integrationRepository.findById(1L)).thenReturn(Optional.of(integration))

        val result = integrationService.findActiveConfigurationIdByIntegrationId(1L)

        assertEquals(1L, result)
    }

    @Test
    fun shouldSaveIntegration() {
        val integrationPostDto = IntegrationPostDto(1L, "integration-1", "destination")
        val integration =
            Integration(
                id = 1L,
                sourceApplicationId = 1L,
                sourceApplicationIntegrationId = "integration-1",
                destination = "destination",
                state = Integration.State.DEACTIVATED,
            )
        whenever(integrationRepository.save(any<Integration>())).thenReturn(integration)

        val result = integrationService.save(integrationPostDto)

        assertEquals(1L, result.id)
        assertEquals("destination", result.destination)
    }

    @Test
    fun shouldUpdateIntegrationById() {
        val integration =
            Integration(
                id = 1L,
                sourceApplicationId = 1L,
                sourceApplicationIntegrationId = "integration-1",
                destination = "destination",
                state = Integration.State.DEACTIVATED,
            )
        val integrationPatchDto =
            IntegrationPatchDto(
                destination = "updated-destination",
                state = Integration.State.ACTIVE,
                activeConfigurationId = 42L,
            )

        whenever(integrationRepository.getReferenceById(1L)).thenReturn(integration)
        whenever(integrationRepository.save(integration)).thenReturn(integration)

        val result = integrationService.updateById(1L, integrationPatchDto)

        assertEquals("updated-destination", result.destination)
        assertEquals(Integration.State.ACTIVE, result.state)
        assertEquals(42L, result.activeConfigurationId)
        verify(integrationRepository).save(integration)
    }
}
