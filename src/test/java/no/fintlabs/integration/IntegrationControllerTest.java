package no.fintlabs.integration;

import no.fintlabs.integration.model.dtos.IntegrationDto;
import no.fintlabs.integration.model.dtos.IntegrationPostDto;
import no.fintlabs.integration.validation.IntegrationValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class IntegrationControllerTest {

    @Mock
    private IntegrationService integrationService;

    @Mock
    private IntegrationValidatorFactory integrationValidatorFactory;

    @InjectMocks
    private IntegrationController controller;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetIntegrations() {
        Authentication mockAuth = mock(Authentication.class);
        when(integrationService.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<Collection<IntegrationDto>> response = controller.getIntegrations(mockAuth);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, Objects.requireNonNull(response.getBody()).size());
        verify(integrationService).findAll();
    }

    @Test
    void testGetIntegrationFound() {
        IntegrationDto mockDto = IntegrationDto.builder().id(1L).destination("destination").build();
        when(integrationService.findById(1L)).thenReturn(Optional.of(mockDto));

        ResponseEntity<IntegrationDto> response = controller.getIntegration(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockDto, response.getBody());

        verify(integrationService).findById(1L);
    }

    @Test
    void testGetIntegrationNotFound() {
        when(integrationService.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> controller.getIntegration(1L));

        verify(integrationService).findById(1L);
    }

    @Test
    void testPostIntegration() {

        Validator mockValidator = mock(Validator.class);
        when(integrationValidatorFactory.getValidator()).thenReturn(mockValidator);

        IntegrationPostDto postDto = IntegrationPostDto.builder().build();
        IntegrationDto mockDto = IntegrationDto.builder().id(1L).destination("destination").build();

        when(integrationService.save(postDto)).thenReturn(mockDto);

        ResponseEntity<IntegrationDto> response = controller.postIntegration(postDto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockDto, response.getBody());

        verify(integrationService).save(postDto);
    }

    @Test
    void testPostIntegrationWithValidationFailure() {
        IntegrationPostDto postDto = IntegrationPostDto.builder().build();
        when(integrationValidatorFactory.getValidator()).thenThrow(ValidationException.class);

        assertThrows(ValidationException.class, () -> controller.postIntegration(postDto));
    }

    // TODO: 18/08/2023 add test for patchIntegration
//    @Test
//    void testPatchIntegration() {
//
//
//
//
//        IntegrationPatchDto patchDto = IntegrationPatchDto.builder().destination("new_destination").build();
//        IntegrationDto existingIntegrationDto = IntegrationDto.builder().id(1L).destination("existing_destination").build();
////        when(existingIntegrationDto.getId()).thenReturn(1L);
////        when(existingIntegrationDto.getDestination()).thenReturn("existing_destination");
//        IntegrationDto patchedDto = IntegrationDto.builder().id(1L).destination("new_destination").build();
//
//        Long mockActiveConfigurationId = 123L;
//
//        when(existingIntegrationDto.getActiveConfigurationId()).thenReturn(mockActiveConfigurationId);
//        Validator mockPatchValidator = mock(Validator.class);
//        when(integrationValidatorFactory.getPatchValidator(1L, mockActiveConfigurationId)).thenReturn(mockPatchValidator);
//
//        when(integrationService.findById(1L)).thenReturn(Optional.of(existingIntegrationDto));
//
//        when(integrationService.updateById(1L, patchDto)).thenReturn(patchedDto);
//
//        ResponseEntity<IntegrationDto> response = controller.patchIntegration(1L, patchDto);
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals(patchedDto, response.getBody());
//
//        verify(integrationService).updateById(1L, patchDto);
//    }

}
