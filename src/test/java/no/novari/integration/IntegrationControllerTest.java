package no.novari.integration;

import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import no.novari.integration.model.dtos.IntegrationDto;
import no.novari.integration.model.dtos.IntegrationPostDto;
import no.novari.integration.model.entities.Integration;
import no.novari.integration.validation.IntegrationValidatorFactory;
import no.fintlabs.resourceserver.security.user.UserAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IntegrationControllerTest {

    @Mock
    private IntegrationService integrationService;

    @Mock
    private IntegrationValidatorFactory integrationValidatorFactory;

    @Mock
    Authentication authentication;

    @Mock
    UserAuthorizationService userAuthorizationService;

    @InjectMocks
    private IntegrationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldReturnAllIntegrationsWithUserPermissionsDisabled() {
        when(integrationService.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<Collection<IntegrationDto>> response = controller.getIntegrations(authentication, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, Objects.requireNonNull(response.getBody()).size());
        verify(integrationService).findAll();
    }

    @Test
    public void shouldReturnSpecificIntegrationsBasedOnProvidedSourceApplicationIdWithUserPermissionsDisabled() {
        when(integrationService.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<Collection<IntegrationDto>> response = controller.getIntegrations(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, Objects.requireNonNull(response.getBody()).size());
        verify(integrationService).findAllBySourceApplicationIds(Set.of(1L));
    }

    @Test
    public void shouldReturnSpecificIntegrationsWithUserPermissionsEnabled() throws NoSuchFieldException, IllegalAccessException {
        setUserPermissionsConsumerEnabled();

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("sourceApplicationIds")).thenReturn("1,2");
        when(authentication.getPrincipal()).thenReturn(jwt);

//        List<Long> sourceApplicationIds = UserAuthorizationUtil.convertSourceApplicationIdsStringToList(authentication);

        Set<Long> authorizedSourceApplicationIds = Set.of(1L, 2L);
        when(userAuthorizationService.getUserAuthorizedSourceApplicationIds(authentication))
                .thenReturn(authorizedSourceApplicationIds);

        IntegrationDto integration1 = IntegrationDto.builder()
                .id(1L)
                .sourceApplicationId(1L)
                .destination("Destination 1")
                .state(Integration.State.ACTIVE)
                .build();

        IntegrationDto integration2 = IntegrationDto.builder()
                .id(2L)
                .sourceApplicationId(2L)
                .destination("Destination 2")
                .state(Integration.State.DEACTIVATED)
                .build();

        List<IntegrationDto> expectedIntegrations = Arrays.asList(integration1, integration2);

        when(integrationService.findAllBySourceApplicationIds(authorizedSourceApplicationIds))
                .thenReturn(expectedIntegrations);

        ResponseEntity<Collection<IntegrationDto>> response = controller.getIntegrations(authentication, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains(integration1));
        assertTrue(response.getBody().contains(integration2));

        verify(integrationService).findAllBySourceApplicationIds(authorizedSourceApplicationIds);
        verify(integrationService, never()).findAll();
    }


    @Test
    void testGetIntegrationFound() {
        IntegrationDto mockDto = IntegrationDto.builder().id(1L).destination("destination").build();
        when(integrationService.findById(1L)).thenReturn(Optional.of(mockDto));

        ResponseEntity<IntegrationDto> response = controller.getIntegration(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockDto, response.getBody());

        verify(integrationService).findById(1L);
    }

    @Test
    void testGetIntegrationNotFound() {
        when(integrationService.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> controller.getIntegration(authentication, 1L));

        verify(integrationService).findById(1L);
    }

    @Test
    void testPostIntegration() {

        Validator mockValidator = mock(Validator.class);
        when(integrationValidatorFactory.getValidator()).thenReturn(mockValidator);

        IntegrationPostDto postDto = IntegrationPostDto.builder().build();
        IntegrationDto mockDto = IntegrationDto.builder().id(1L).destination("destination").build();

        when(integrationService.save(postDto)).thenReturn(mockDto);

        ResponseEntity<IntegrationDto> response = controller.postIntegration(authentication, postDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockDto, response.getBody());

        verify(integrationService).save(postDto);
    }

    @Test
    void testPostIntegrationWithValidationFailure() {
        IntegrationPostDto postDto = IntegrationPostDto.builder().build();
        when(integrationValidatorFactory.getValidator()).thenThrow(ValidationException.class);

        assertThrows(ValidationException.class, () -> controller.postIntegration(authentication, postDto));
    }

    private void setUserPermissionsConsumerEnabled() throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = IntegrationController.class.getDeclaredField("userPermissionsConsumerEnabled");
        field.setAccessible(true);
        field.set(controller, true);
    }

}
