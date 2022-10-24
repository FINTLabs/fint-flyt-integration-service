package no.fintlabs.integration.validation.constraints;

import no.fintlabs.integration.validation.IntegrationValidationContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

public class ReferencedConfigurationExistsValidator
        implements HibernateConstraintValidator<ReferencedConfigurationExists, Long> {

    @Override
    public boolean isValid(Long value, HibernateConstraintValidatorContext hibernateConstraintValidatorContext) {
        IntegrationValidationContext integrationValidationContext = hibernateConstraintValidatorContext
                .getConstraintValidatorPayload(IntegrationValidationContext.class);

        return value == null || integrationValidationContext.getConfiguration() != null;
    }

}
