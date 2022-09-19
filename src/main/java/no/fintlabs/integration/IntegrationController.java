package no.fintlabs.integration;

import no.fintlabs.integration.model.ActiveConfigurationIdWrapper;
import no.fintlabs.integration.model.Integration;
import no.fintlabs.integration.model.IntegrationState;
import no.fintlabs.integration.model.IntegrationStateWrapper;
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

    private final IntegrationRepository integrationRepository;
    private final ValidatorService validatorService;

    public IntegrationController(IntegrationRepository integrationRepository, ValidatorService validatorService) {
        this.integrationRepository = integrationRepository;
        this.validatorService = validatorService;
    }

    @GetMapping
    public ResponseEntity<Collection<Integration>> getIntegrations() {
        return ResponseEntity.ok(integrationRepository.findAll());
    }

    @GetMapping("{integrationId}")
    public ResponseEntity<Integration> getIntegration(
            @PathVariable Long integrationId
    ) {
        Integration integration = integrationRepository
                .findById(integrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(integration);
    }

    @PostMapping
    public ResponseEntity<Integration> postIntegration(
            @RequestBody Integration integration
    ) {
        validatorService.validate(integration).ifPresent(this::createValidationErrorResponse);

        if (integrationRepository.existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
                integration.getSourceApplicationId(),
                integration.getSourceApplicationIntegrationId()
        )) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok(integrationRepository.save(integration));
    }

    @PutMapping("{integrationId}/tilstand")
    public ResponseEntity<IntegrationStateWrapper> putState(
            @PathVariable Long integrationId,
            @RequestBody IntegrationStateWrapper integrationStateWrapper
    ) {
        validatorService.validate(integrationStateWrapper).ifPresent(this::createValidationErrorResponse);

        Integration integration = integrationRepository
                .findById(integrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        integration.setState(integrationStateWrapper.getState());
        IntegrationState resultState = integrationRepository.save(integration).getState();
        return ResponseEntity.ok(
                new IntegrationStateWrapper(resultState)
        );
    }

    @PutMapping("{integrationId}/aktiv-konfigurasjon-id")
    public ResponseEntity<ActiveConfigurationIdWrapper> putActiveConfigurationId(
            @PathVariable Long integrationId,
            @RequestBody ActiveConfigurationIdWrapper activeConfigurationIdWrapper
    ) {
        validatorService.validate(activeConfigurationIdWrapper).ifPresent(this::createValidationErrorResponse);

        Integration integration = integrationRepository
                .findById(integrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        integration.setActiveConfigurationId(activeConfigurationIdWrapper.getActiveConfigurationId());
        String activeConfigurationIdResult = integrationRepository.save(integration).getActiveConfigurationId();
        return ResponseEntity.ok(
                new ActiveConfigurationIdWrapper(activeConfigurationIdResult)
        );
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
