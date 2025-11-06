package no.fintlabs.integration;

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.integration.model.dtos.IntegrationDto;
import no.fintlabs.integration.model.dtos.IntegrationPatchDto;
import no.fintlabs.integration.model.dtos.IntegrationPostDto;
import no.fintlabs.integration.validation.IntegrationValidatorFactory;
import no.fintlabs.integration.validation.ValidationErrorsFormattingService;
import no.fintlabs.resourceserver.security.user.UserAuthorizationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Set;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/integrasjoner")
@Slf4j
public class IntegrationController {

    private final IntegrationService integrationService;
    private final IntegrationValidatorFactory integrationValidatorFactory;
    private final ValidationErrorsFormattingService validationErrorsFormattingService;
    private final UserAuthorizationService userAuthorizationService;
    @Value("${fint.flyt.resource-server.user-permissions-consumer.enabled:false}")
    private boolean userPermissionsConsumerEnabled;

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
        if (userPermissionsConsumerEnabled) {
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

        if (sourceApplicationId != null) {
            return ResponseEntity.ok(integrationService.findAllBySourceApplicationIds(Set.of(sourceApplicationId)));
        }

        return ResponseEntity.ok(integrationService.findAll());
    }

    private ResponseEntity<Page<IntegrationDto>> getResponseEntityIntegrations(
            Authentication authentication,
            Pageable pageable,
            Long sourceApplicationId
    ) {
        if (userPermissionsConsumerEnabled) {
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
        return ResponseEntity.ok(integrationService.findAll(pageable));
    }

    @GetMapping("{integrationId}")
    public ResponseEntity<IntegrationDto> getIntegration(
            @AuthenticationPrincipal Authentication authentication,
            @PathVariable Long integrationId
    ) {
        IntegrationDto integrationDto = integrationService.findById(integrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (userPermissionsConsumerEnabled) {
            userAuthorizationService.checkIfUserHasAccessToSourceApplication(
                    authentication,
                    integrationDto.getSourceApplicationId());
        }

        return ResponseEntity.ok(integrationDto);
    }

    @PostMapping
    public ResponseEntity<IntegrationDto> postIntegration(
            @AuthenticationPrincipal Authentication authentication,
            @RequestBody IntegrationPostDto integrationPostDto
    ) {
        if (userPermissionsConsumerEnabled) {
            userAuthorizationService.checkIfUserHasAccessToSourceApplication(
                    authentication,
                    integrationPostDto.getSourceApplicationId());
        }

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

        if (userPermissionsConsumerEnabled) {
            userAuthorizationService.checkIfUserHasAccessToSourceApplication(authentication, existingIntegration.getSourceApplicationId());
        }

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
