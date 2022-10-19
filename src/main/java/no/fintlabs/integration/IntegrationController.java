package no.fintlabs.integration;

import no.fintlabs.integration.model.dtos.IntegrationDto;
import no.fintlabs.integration.model.dtos.IntegrationPatchDto;
import no.fintlabs.integration.model.dtos.IntegrationPostDto;
import no.fintlabs.integration.validation.IntegrationValidatorFacory;
import no.fintlabs.integration.validation.ValidationErrorsFormattingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolation;
import java.util.Collection;
import java.util.Set;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/integrasjoner")
public class IntegrationController {

    private final IntegrationService integrationService;
    private final IntegrationValidatorFacory integrationValidatorFacory;
    private final ValidationErrorsFormattingService validationErrorsFormattingService;

    public IntegrationController(
            IntegrationService integrationService,
            IntegrationValidatorFacory integrationValidatorFacory,
            ValidationErrorsFormattingService validationErrorsFormattingService
    ) {
        this.integrationService = integrationService;
        this.integrationValidatorFacory = integrationValidatorFacory;
        this.validationErrorsFormattingService = validationErrorsFormattingService;
    }

    @GetMapping
    public ResponseEntity<Collection<IntegrationDto>> getIntegrations() {
        return ResponseEntity.ok(integrationService.findAll());
    }

    @GetMapping("{integrationId}")
    public ResponseEntity<IntegrationDto> getIntegration(
            @PathVariable Long integrationId
    ) {
        IntegrationDto integrationDto = integrationService
                .findById(integrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(integrationDto);
    }

    @PostMapping
    public ResponseEntity<IntegrationDto> postIntegration(
            @RequestBody IntegrationPostDto integrationPostDto
    ) {
        validatePost(integrationPostDto);
        return ResponseEntity.ok(integrationService.save(integrationPostDto));
    }

    private void validatePost(IntegrationPostDto integrationPostDto) {
        Set<ConstraintViolation<IntegrationPostDto>> constraintViolations = integrationValidatorFacory
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
        if (!integrationService.existsById(integrationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        validatePatch(integrationId, integrationPatchDto);
        return ResponseEntity.ok(integrationService.updateById(integrationId, integrationPatchDto));
    }

    private void validatePatch(Long integrationId, IntegrationPatchDto integrationPatchDto) {
        Set<ConstraintViolation<IntegrationPatchDto>> constraintViolations = integrationValidatorFacory
                .getPatchValidator(integrationId, integrationPatchDto.getActiveConfigurationId().orElse(null))
                .validate(integrationPatchDto);
        if (!constraintViolations.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    validationErrorsFormattingService.format(constraintViolations)
            );
        }
    }

}
