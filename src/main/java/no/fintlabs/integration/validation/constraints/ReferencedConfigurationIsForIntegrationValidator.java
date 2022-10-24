package no.fintlabs.integration.validation.constraints;

import no.fintlabs.integration.validation.IntegrationValidationContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.Objects;

public class ReferencedConfigurationIsForIntegrationValidator
        implements HibernateConstraintValidator<ReferencedConfigurationIsForIntegration, Long> {

    @Override
    public boolean isValid(Long value, HibernateConstraintValidatorContext hibernateConstraintValidatorContext) {
        IntegrationValidationContext integrationValidationContext = hibernateConstraintValidatorContext
                .getConstraintValidatorPayload(IntegrationValidationContext.class);

        return value == null ||
                Objects.equals(
                        integrationValidationContext.getConfiguration().getIntegrationId(),
                        integrationValidationContext.getIntegrationId()
                );
    }

}
