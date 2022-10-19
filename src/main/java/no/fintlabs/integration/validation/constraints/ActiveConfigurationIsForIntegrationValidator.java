package no.fintlabs.integration.validation.constraints;

import no.fintlabs.integration.model.dtos.IntegrationPatchDto;
import no.fintlabs.integration.validation.IntegrationValidationContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.Objects;

public class ActiveConfigurationIsForIntegrationValidator
        implements HibernateConstraintValidator<ActiveConfigurationIsForIntegration, IntegrationPatchDto> {

    @Override
    public boolean isValid(IntegrationPatchDto value, HibernateConstraintValidatorContext hibernateConstraintValidatorContext) {
        IntegrationValidationContext integrationValidationContext = hibernateConstraintValidatorContext
                .getConstraintValidatorPayload(IntegrationValidationContext.class);

        return value.getActiveConfigurationId().isEmpty() ||
                Objects.equals(
                        integrationValidationContext.getConfiguration().getIntegrationId(),
                        integrationValidationContext.getIntegrationId()
                );
    }

}
