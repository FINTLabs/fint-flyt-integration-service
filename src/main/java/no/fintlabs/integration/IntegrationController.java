package no.fintlabs.integration;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.integration.model.dtos.IntegrationDto;
import no.fintlabs.integration.model.dtos.IntegrationPatchDto;
import no.fintlabs.integration.model.dtos.IntegrationPostDto;
import no.fintlabs.integration.validation.IntegrationValidatorFactory;
import no.fintlabs.integration.validation.ValidationErrorsFormattingService;
import no.fintlabs.resourceserver.security.client.FintFlytJwtUserConverterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolation;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/integrasjoner")
@Slf4j
public class IntegrationController {

    private final IntegrationService integrationService;
    private final IntegrationValidatorFactory integrationValidatorFactory;
    private final ValidationErrorsFormattingService validationErrorsFormattingService;
    private final FintFlytJwtUserConverterService fintFlytJwtUserConverterService;
    @Value("${fint.flyt.resource-server.user-permissions-consumer.enabled:false}")
    private boolean userPermissionsConsumerEnabled;

    public IntegrationController(
            IntegrationService integrationService,
            IntegrationValidatorFactory integrationValidatorFactory,
            ValidationErrorsFormattingService validationErrorsFormattingService, FintFlytJwtUserConverterService fintFlytJwtUserConverterService
    ) {
        this.integrationService = integrationService;
        this.integrationValidatorFactory = integrationValidatorFactory;
        this.validationErrorsFormattingService = validationErrorsFormattingService;
        this.fintFlytJwtUserConverterService = fintFlytJwtUserConverterService;
    }

    @GetMapping
    public ResponseEntity<Collection<IntegrationDto>> getIntegrations(
            @AuthenticationPrincipal Authentication authentication
    ) {
        return getResponseEntityIntegrations(authentication);
    }

    @GetMapping(params = {"side", "antall", "sorteringFelt", "sorteringRetning"})
    public ResponseEntity<Page<IntegrationDto>> getIntegrations(
            @AuthenticationPrincipal Authentication authentication,
            @RequestParam(name = "side") int page,
            @RequestParam(name = "antall") int size,
            @RequestParam(name = "sorteringFelt") String sortProperty,
            @RequestParam(name = "sorteringRetning") Sort.Direction sortDirection
    ) {
        PageRequest pageRequest = PageRequest
                .of(page, size)
                .withSort(sortDirection, sortProperty);

        return getResponseEntityIntegrations(authentication, pageRequest);
    }

    private ResponseEntity<Collection<IntegrationDto>> getResponseEntityIntegrations(
            Authentication authentication
    ) {
        if (userPermissionsConsumerEnabled) {
            List<Long> sourceApplicationIds = fintFlytJwtUserConverterService
                    .convertSourceApplicationIdsStringToList(authentication);

            Collection<IntegrationDto> allBySourceApplicationIds = integrationService.findAllBySourceApplicationIds(sourceApplicationIds);

            return ResponseEntity.ok(allBySourceApplicationIds);
        }
        return ResponseEntity.ok(integrationService.findAll());
    }

    private ResponseEntity<Page<IntegrationDto>> getResponseEntityIntegrations(
            Authentication authentication,
            Pageable pageable
    ) {
        if (userPermissionsConsumerEnabled) {
            List<Long> sourceApplicationIds = fintFlytJwtUserConverterService
                    .convertSourceApplicationIdsStringToList(authentication);

            Page<IntegrationDto> allBySourceApplicationIds = integrationService.findAllBySourceApplicationIds(sourceApplicationIds, pageable);

            return ResponseEntity.ok(allBySourceApplicationIds);
        }
        return ResponseEntity.ok(integrationService.findAll(pageable));
    }

    @GetMapping("{integrationId}")
    public ResponseEntity<IntegrationDto> getIntegration(@PathVariable Long integrationId) {
        IntegrationDto integrationDto = integrationService
                .findById(integrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(integrationDto);
    }

    @PostMapping
    public ResponseEntity<IntegrationDto> postIntegration(@RequestBody IntegrationPostDto integrationPostDto) {
        validatePost(integrationPostDto);
        return ResponseEntity.ok(integrationService.save(integrationPostDto));
    }

    private void validatePost(IntegrationPostDto integrationPostDto) {
        Set<ConstraintViolation<IntegrationPostDto>> constraintViolations = integrationValidatorFactory
                .getValidator()
                .validate(integrationPostDto);
        if (!constraintViolations.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    validationErrorsFormattingService.format(constraintViolations)
            );
        }

        if (integrationService.existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
                integrationPostDto.getSourceApplicationId(),
                integrationPostDto.getSourceApplicationIntegrationId()
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }

    @PatchMapping("{integrationId}")
    public ResponseEntity<IntegrationDto> patchIntegration(
            @PathVariable Long integrationId,
            @RequestBody IntegrationPatchDto integrationPatchDto
    ) {
        IntegrationDto.IntegrationDtoBuilder integrationDtoBuilder = integrationService.findById(integrationId)
                .map(IntegrationDto::toBuilder)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        integrationPatchDto.getDestination().ifPresent(integrationDtoBuilder::destination);
        integrationPatchDto.getState().ifPresent(integrationDtoBuilder::state);
        integrationPatchDto.getActiveConfigurationId().ifPresent(integrationDtoBuilder::activeConfigurationId);

        validatePatchResult(integrationId, integrationDtoBuilder.build());

        return ResponseEntity.ok(integrationService.updateById(integrationId, integrationPatchDto));
    }

    private void validatePatchResult(Long integrationId, IntegrationDto integrationDto) {
        Set<ConstraintViolation<IntegrationDto>> constraintViolations = integrationValidatorFactory
                .getPatchValidator(
                        integrationId,
                        integrationDto.getActiveConfigurationId()
                )
                .validate(integrationDto);
        if (!constraintViolations.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    validationErrorsFormattingService.format(constraintViolations)
            );
        }
    }

}
