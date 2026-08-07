package no.novari.flyt.integration.api

import jakarta.validation.Valid
import no.novari.flyt.integration.api.dto.IntegrationDto
import no.novari.flyt.integration.api.dto.IntegrationPageResponse
import no.novari.flyt.integration.api.dto.IntegrationPatchDto
import no.novari.flyt.integration.api.dto.IntegrationPostDto
import no.novari.flyt.integration.application.IntegrationService
import no.novari.flyt.integration.application.IntegrationUpdateValidationService
import no.novari.flyt.integration.web.ConflictException
import no.novari.flyt.integration.web.ForbiddenWithoutBodyException
import no.novari.flyt.integration.web.NotFoundException
import no.novari.flyt.webresourceserver.UrlPaths.INTERNAL_API
import no.novari.flyt.webresourceserver.security.user.UserAuthorizationService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("$INTERNAL_API/integrasjoner")
class IntegrationController(
    private val integrationService: IntegrationService,
    private val integrationUpdateValidationService: IntegrationUpdateValidationService,
    private val userAuthorizationService: UserAuthorizationService,
) {
    @GetMapping
    fun listIntegrations(
        authentication: Authentication,
        @RequestParam(required = false) sourceApplicationId: Long?,
    ): Collection<IntegrationDto> {
        val sourceApplicationIds = resolveAuthorizedSourceApplicationIds(authentication, sourceApplicationId)
        return integrationService.findAllBySourceApplicationIds(sourceApplicationIds)
    }

    @GetMapping(params = ["side", "antall", "sorteringFelt", "sorteringRetning"])
    fun listIntegrationsPaginated(
        authentication: Authentication,
        @RequestParam(name = "side") page: Int,
        @RequestParam(name = "antall") size: Int,
        @RequestParam(name = "sorteringFelt") sortProperty: String,
        @RequestParam(name = "sorteringRetning") sortDirection: Sort.Direction,
        @RequestParam(required = false) sourceApplicationId: Long?,
    ): IntegrationPageResponse {
        val sourceApplicationIds = resolveAuthorizedSourceApplicationIds(authentication, sourceApplicationId)
        val pageRequest = PageRequest.of(page, size).withSort(sortDirection, sortProperty)
        val integrations = integrationService.findAllBySourceApplicationIds(sourceApplicationIds, pageRequest)
        return IntegrationPageResponse(
            content = integrations.content,
            totalElements = integrations.totalElements,
            totalPages = integrations.totalPages,
        )
    }

    @GetMapping("{integrationId}")
    fun getIntegrationById(
        authentication: Authentication,
        @PathVariable integrationId: Long,
    ): IntegrationDto {
        val integration =
            integrationService.findById(integrationId)
                ?: throw NotFoundException()

        userAuthorizationService.checkIfUserHasAccessToSourceApplication(
            authentication,
            integration.sourceApplicationId,
        )

        return integration
    }

    @PostMapping
    fun createIntegration(
        authentication: Authentication,
        @Valid @RequestBody integrationPostDto: IntegrationPostDto,
    ): IntegrationDto {
        ensureIntegrationDoesNotAlreadyExist(integrationPostDto)

        userAuthorizationService.checkIfUserHasAccessToSourceApplication(
            authentication,
            requireNotNull(integrationPostDto.sourceApplicationId),
        )

        return integrationService.save(integrationPostDto)
    }

    @PatchMapping("{integrationId}")
    fun updateIntegration(
        authentication: Authentication,
        @PathVariable integrationId: Long,
        @RequestBody integrationPatchDto: IntegrationPatchDto,
    ): IntegrationDto {
        val existingIntegration =
            integrationService.findById(integrationId)
                ?: throw NotFoundException()

        userAuthorizationService.checkIfUserHasAccessToSourceApplication(
            authentication,
            existingIntegration.sourceApplicationId,
        )

        val updatedIntegration =
            existingIntegration.copy(
                destination = integrationPatchDto.destination ?: existingIntegration.destination,
                state = integrationPatchDto.state ?: existingIntegration.state,
                activeConfigurationId =
                    integrationPatchDto.activeConfigurationId ?: existingIntegration.activeConfigurationId,
            )

        integrationUpdateValidationService.validate(integrationId, updatedIntegration)

        return integrationService.updateById(integrationId, integrationPatchDto)
    }

    private fun resolveAuthorizedSourceApplicationIds(
        authentication: Authentication,
        sourceApplicationId: Long?,
    ): Set<Long> {
        val candidateSourceApplicationIds =
            sourceApplicationId?.let(::setOf) ?: integrationService.findDistinctSourceApplicationIds()
        val authorizedSourceApplicationIds =
            userAuthorizationService.getUserAuthorizedSourceApplicationIds(
                authentication,
                candidateSourceApplicationIds,
            )

        if (sourceApplicationId != null && !authorizedSourceApplicationIds.contains(sourceApplicationId)) {
            throw ForbiddenWithoutBodyException()
        }

        return authorizedSourceApplicationIds
    }

    private fun ensureIntegrationDoesNotAlreadyExist(integrationPostDto: IntegrationPostDto) {
        if (
            integrationService.existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
                requireNotNull(integrationPostDto.sourceApplicationId),
                requireNotNull(integrationPostDto.sourceApplicationIntegrationId),
            )
        ) {
            throw ConflictException()
        }
    }
}
