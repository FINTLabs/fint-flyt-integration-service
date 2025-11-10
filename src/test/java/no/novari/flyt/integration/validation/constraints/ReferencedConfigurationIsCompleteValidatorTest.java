package no.novari.flyt.integration.validation.constraints;

import no.novari.flyt.integration.model.dtos.ConfigurationDto;
import no.novari.flyt.integration.validation.IntegrationValidationContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReferencedConfigurationIsCompleteValidatorTest {

    private ReferencedConfigurationIsCompleteValidator validator;
    private IntegrationValidationContext validationContext;

    @BeforeEach
    void setUp() {
        validator = new ReferencedConfigurationIsCompleteValidator();
        validationContext = mock(IntegrationValidationContext.class);
    }

    @Test
    void testIsValid_NullValue_ReturnsTrue() {
        boolean result = validator.isValid(null, createMockContext());

        assertTrue(result);
    }

    @Test
    void testIsValid_ConfigurationCompleted_ReturnsTrue() {
        when(validationContext.getConfiguration()).thenReturn(
                ConfigurationDto.builder()
                        .completed(true)
                        .build());
        HibernateConstraintValidatorContext mockContext = createMockContext();

        boolean result = validator.isValid(1L, mockContext);

        assertTrue(result);
    }

    @Test
    void testIsValid_ConfigurationNotCompleted_ReturnsFalse() {
        when(validationContext.getConfiguration()).thenReturn(
                ConfigurationDto.builder()
                        .completed(false)
                        .build()
        );
        HibernateConstraintValidatorContext mockContext = createMockContext();

        boolean result = validator.isValid(1L, mockContext);

        assertFalse(result);
    }

    private HibernateConstraintValidatorContext createMockContext() {
        HibernateConstraintValidatorContext mockContext = mock(HibernateConstraintValidatorContext.class);
        when(mockContext.getConstraintValidatorPayload(IntegrationValidationContext.class))
                .thenReturn(validationContext);
        return mockContext;
    }

}
