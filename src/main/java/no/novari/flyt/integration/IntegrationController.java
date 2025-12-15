package no.novari.flyt.integration;

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.integration.model.dtos.IntegrationDto;
import no.novari.flyt.integration.model.dtos.IntegrationPatchDto;
import no.novari.flyt.integration.model.dtos.IntegrationPostDto;
import no.novari.flyt.integration.validation.IntegrationValidatorFactory;
import no.novari.flyt.integration.validation.ValidationErrorsFormattingService;
import no.novari.flyt.resourceserver.security.user.UserAuthorizationService;
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

import java.util.Collection;
import java.util.Set;

import static no.novari.flyt.resourceserver.UrlPaths.INTERNAL_API;


@RestController
@RequestMapping(INTERNAL_API + "/integrasjoner")
@Slf4j
public class IntegrationController {

    private final IntegrationService integrationService;
    private final IntegrationValidatorFactory integrationValidatorFactory;
    private final ValidationErrorsFormattingService validationErrorsFormattingService;
    private final UserAuthorizationService userAuthorizationService;

    public IntegrationController(
            IntegrationService integrationService,
            IntegrationValidatorFactory integrationValidatorFactory,
            ValidationErrorsFormattingService validationErrorsFormattingService,
            UserAuthorizationService userAuthorizationService) {
        this.integrationService = integrationService;
        this.integrationValidatorFactory = integrationValidatorFactory;
        this.validationErrorsFormattingService = validationErrorsFormattingService;
        this.userAuthorizationService = userAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<Collection<IntegrationDto>> getIntegrations(
            @AuthenticationPrincipal Authentication authentication,
            @RequestParam(required = false) Long sourceApplicationId
    ) {
        return getResponseEntityIntegrations(authentication, sourceApplicationId);
    }

    @GetMapping(params = {"side", "antall", "sorteringFelt", "sorteringRetning"})
    public ResponseEntity<Page<IntegrationDto>> getIntegrations(
            @AuthenticationPrincipal Authentication authentication,
            @RequestParam(name = "side") int page,
            @RequestParam(name = "antall") int size,
            @RequestParam(name = "sorteringFelt") String sortProperty,
            @RequestParam(name = "sorteringRetning") Sort.Direction sortDirection,
            @RequestParam(required = false) Long sourceApplicationId
    ) {
        PageRequest pageRequest = PageRequest
                .of(page, size)
                .withSort(sortDirection, sortProperty);

        return getResponseEntityIntegrations(authentication, pageRequest, sourceApplicationId);
    }

    private ResponseEntity<Collection<IntegrationDto>> getResponseEntityIntegrations(
            Authentication authentication,
            Long sourceApplicationId
    ) {
        Set<Long> sourceApplicationIds =
                userAuthorizationService.getUserAuthorizedSourceApplicationIds(authentication);

        if (sourceApplicationId != null) {
            if (!sourceApplicationIds.contains(sourceApplicationId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(integrationService.findAllBySourceApplicationIds(Set.of(sourceApplicationId)));
        }

        return ResponseEntity.ok(integrationService.findAllBySourceApplicationIds(sourceApplicationIds));

    }

    private ResponseEntity<Page<IntegrationDto>> getResponseEntityIntegrations(
            Authentication authentication,
            Pageable pageable,
            Long sourceApplicationId
    ) {
        Set<Long> sourceApplicationIds =
                userAuthorizationService.getUserAuthorizedSourceApplicationIds(authentication);

        if (sourceApplicationId != null) {
            if (!sourceApplicationIds.contains(sourceApplicationId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Page<IntegrationDto> allBySourceApplicationId = integrationService.findAllBySourceApplicationIds(
                    Set.of(sourceApplicationId), pageable
            );
            return ResponseEntity.ok(allBySourceApplicationId);
        }

        Page<IntegrationDto> allBySourceApplicationIds = integrationService.findAllBySourceApplicationIds(
                sourceApplicationIds, pageable
        );
        return ResponseEntity.ok(allBySourceApplicationIds);
    }

    @GetMapping("{integrationId}")
    public ResponseEntity<IntegrationDto> getIntegration(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long integrationId
    ) {
        IntegrationDto integrationDto = integrationService.findById(integrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        userAuthorizationService.checkIfUserHasAccessToSourceApplication(
                authentication,
                integrationDto.getSourceApplicationId());

        return ResponseEntity.ok(integrationDto);
    }

    @PostMapping
    public ResponseEntity<IntegrationDto> postIntegration(
            @AuthenticationPrincipal Authentication authentication,
            @RequestBody IntegrationPostDto integrationPostDto
    ) {
        userAuthorizationService.checkIfUserHasAccessToSourceApplication(
                authentication,
                integrationPostDto.getSourceApplicationId());

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
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long integrationId,
            @RequestBody IntegrationPatchDto integrationPatchDto
    ) {
        IntegrationDto existingIntegration = integrationService.findById(integrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        userAuthorizationService.checkIfUserHasAccessToSourceApplication(authentication, existingIntegration.getSourceApplicationId());

        IntegrationDto.IntegrationDtoBuilder integrationDtoBuilder = existingIntegration.toBuilder();

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
