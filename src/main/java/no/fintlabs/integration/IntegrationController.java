package no.fintlabs.integration;

import no.fintlabs.integration.model.dtos.IntegrationDto;
import no.fintlabs.integration.model.dtos.IntegrationPatchDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/integrasjoner")
public class IntegrationController {

    private final IntegrationService integrationService;
    private final ValidatorService validatorService;

    public IntegrationController(IntegrationService integrationService, ValidatorService validatorService) {
        this.integrationService = integrationService;
        this.validatorService = validatorService;
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
            @RequestBody IntegrationDto integrationDto
    ) {
        validatorService.validate(integrationDto).ifPresent(this::createValidationErrorResponse);

        if (integrationService.existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
                integrationDto.getSourceApplicationId(),
                integrationDto.getSourceApplicationIntegrationId()
        )) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok(integrationService.save(integrationDto));
    }

    @PatchMapping("{integrationId}")
    public ResponseEntity<IntegrationDto> patchIntegration(
            @PathVariable Long integrationId,
            @RequestBody IntegrationPatchDto integrationPatchDto
    ) {
        IntegrationDto integrationDto = integrationService
                .findById(integrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        integrationPatchDto.getDestination().ifPresent(integrationDto::setDestination);
        integrationPatchDto.getState().ifPresent(integrationDto::setState);
        integrationPatchDto.getActiveConfigurationId().ifPresent(integrationDto::setActiveConfigurationId);

        validatorService.validate(integrationDto).ifPresent(this::createValidationErrorResponse);

        return ResponseEntity.ok(integrationService.updateById(integrationId, integrationPatchDto));
    }

    private void createValidationErrorResponse(List<ValidatorService.Error> validationErrors) {
        throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Validation error" + (validationErrors.size() > 1 ? "s:" : ": ") +
                        validationErrors
                                .stream()
                                .map(error -> "'" + error.getFieldPath() + " " + error.getErrorMessage() + "'")
                                .toList()
        );
    }

}
