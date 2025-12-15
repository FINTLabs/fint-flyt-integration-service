package no.novari.flyt.integration.validation.constraints;

import no.novari.flyt.integration.model.dtos.ConfigurationDto;
import no.novari.flyt.integration.model.entities.Integration;
import no.novari.flyt.integration.validation.IntegrationValidationContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActiveConfigurationIsDefinedIfStateIsActiveValidatorTest {

    private ActiveConfigurationIsDefinedIfStateIsActiveValidator validator;
    private HibernateConstraintValidatorContext context;
    private IntegrationValidationContext validationContext;

    @BeforeEach
    public void setUp() {
        validator = new ActiveConfigurationIsDefinedIfStateIsActiveValidator();
        context = mock(ConstraintValidatorContextImpl.class);
        validationContext = mock(IntegrationValidationContext.class);
    }

    @Test
    public void shouldReturnTrueWhenStateIsNotActive() {
        when(context.getConstraintValidatorPayload(IntegrationValidationContext.class))
                .thenReturn(validationContext);

        boolean result = validator.isValid(Integration.State.DEACTIVATED, context);

        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueWhenStateIsActiveAndConfigurationIsNotNull() {
        when(context.getConstraintValidatorPayload(IntegrationValidationContext.class))
                .thenReturn(validationContext);
        when(validationContext.getConfiguration()).thenReturn(
                ConfigurationDto.builder()
                        .id(1L)
                        .integrationId(2L)
                        .integrationMetadataId(3L)
                        .completed(true)
                        .comment("Test Comment")
                        .version(1)
                        .build());

        boolean result = validator.isValid(Integration.State.ACTIVE, context);

        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenStateIsActiveAndConfigurationIsNull() {
        when(context.getConstraintValidatorPayload(IntegrationValidationContext.class))
                .thenReturn(validationContext);
        when(validationContext.getConfiguration()).thenReturn(null);

        boolean result = validator.isValid(Integration.State.ACTIVE, context);

        assertFalse(result);
    }

}
