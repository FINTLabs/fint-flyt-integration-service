package no.fintlabs.integration.validation.constraints;

import no.fintlabs.integration.model.dtos.ConfigurationDto;
import no.fintlabs.integration.validation.IntegrationValidationContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReferencedConfigurationExistsValidatorTest {

    @Test
    public void testValidator() {
        HibernateConstraintValidatorContext context = mock(HibernateConstraintValidatorContext.class);
        IntegrationValidationContext validationContext = mock(IntegrationValidationContext.class);

        ReferencedConfigurationExistsValidator validator = new ReferencedConfigurationExistsValidator();

        when(context.getConstraintValidatorPayload(IntegrationValidationContext.class)).thenReturn(validationContext);
        assertTrue(validator.isValid(null, context));

        when(context.getConstraintValidatorPayload(IntegrationValidationContext.class)).thenReturn(validationContext);
        assertFalse(validator.isValid(1L, context));

        when(validationContext.getConfiguration()).thenReturn(
                ConfigurationDto.builder()
                        .id(1L)
                        .integrationId(2L)
                        .integrationMetadataId(3L)
                        .completed(true)
                        .comment("Test Comment")
                        .version(1)
                        .build());
        assertTrue(validator.isValid(1L, context));
    }
}
