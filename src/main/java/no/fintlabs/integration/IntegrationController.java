package no.fintlabs.integration;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.integration.model.dtos.IntegrationDto;
import no.fintlabs.integration.model.dtos.IntegrationPatchDto;
import no.fintlabs.integration.model.dtos.IntegrationPostDto;
import no.fintlabs.integration.validation.IntegrationValidatorFacory;
import no.fintlabs.integration.validation.ValidationErrorsFormattingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
@Slf4j
public class IntegrationController {

    private final IntegrationService integrationService;
    private final IntegrationValidatorFacory integrationValidatorFacory;
    private final ValidationErrorsFormattingService validationErrorsFormattingService;

    public IntegrationController(
            IntegrationService integrationService,
            IntegrationValidatorFacory integrationValidatorFacory,
            ValidationErrorsFormattingService validationErrorsFormattingService
    ) {
        log.info("did the request reach me?");

        this.integrationService = integrationService;
        this.integrationValidatorFacory = integrationValidatorFacory;
        this.validationErrorsFormattingService = validationErrorsFormattingService;
    }

    @GetMapping()
    public ResponseEntity<Collection<IntegrationDto>> getIntegrations() {
        log.info("get integrations 1");

        return ResponseEntity.ok(integrationService.findAll());
    }

    @GetMapping(params = {"side", "antall", "sorteringFelt", "sorteringRetning"})
    public ResponseEntity<Page<IntegrationDto>> getIntegrations(
            @RequestParam(name = "side") int page,
            @RequestParam(name = "antall") int size,
            @RequestParam(name = "sorteringFelt") String sortProperty,
            @RequestParam(name = "sorteringRetning") Sort.Direction sortDirection
    ) {
        log.info("get integrations 2");

        PageRequest pageRequest = PageRequest
                .of(page, size)
                .withSort(sortDirection, sortProperty);

        return ResponseEntity.ok(integrationService.findAll(pageRequest));
    }

    @GetMapping("{integrationId}")
    public ResponseEntity<IntegrationDto> getIntegration(@PathVariable Long integrationId) {
        log.info("get integration");

        IntegrationDto integrationDto = integrationService
                .findById(integrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(integrationDto);
    }

    @PostMapping
    public ResponseEntity<IntegrationDto> postIntegration(@RequestBody IntegrationPostDto integrationPostDto) {
        log.info("post integration");

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
        log.info("patch integration");

        IntegrationDto integrationDto = integrationService.findById(integrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        integrationPatchDto.getDestination().ifPresent(integrationDto::setDestination);
        integrationPatchDto.getState().ifPresent(integrationDto::setState);
        integrationPatchDto.getActiveConfigurationId().ifPresent(integrationDto::setActiveConfigurationId);

        validatePatchResult(integrationId, integrationDto);

        return ResponseEntity.ok(integrationService.updateById(integrationId, integrationPatchDto));
    }

    private void validatePatchResult(Long integrationId, IntegrationDto integrationDto) {
        Set<ConstraintViolation<IntegrationDto>> constraintViolations = integrationValidatorFacory
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
