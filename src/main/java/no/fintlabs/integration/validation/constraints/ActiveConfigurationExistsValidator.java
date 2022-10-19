package no.fintlabs.integration.validation.constraints;

import no.fintlabs.integration.model.dtos.IntegrationPatchDto;
import no.fintlabs.integration.validation.IntegrationValidationContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

public class ActiveConfigurationExistsValidator
        implements HibernateConstraintValidator<ActiveConfigurationExists, IntegrationPatchDto> {

    @Override
    public boolean isValid(IntegrationPatchDto value, HibernateConstraintValidatorContext hibernateConstraintValidatorContext) {
        IntegrationValidationContext integrationValidationContext = hibernateConstraintValidatorContext
                .getConstraintValidatorPayload(IntegrationValidationContext.class);

        return value.getActiveConfigurationId().isEmpty() ||
                integrationValidationContext.getConfiguration() != null;
    }

}
