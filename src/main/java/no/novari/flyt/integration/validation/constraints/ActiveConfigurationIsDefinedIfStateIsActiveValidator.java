package no.novari.flyt.integration.validation.constraints;

import no.novari.flyt.integration.model.entities.Integration;
import no.novari.flyt.integration.validation.IntegrationValidationContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

public class ActiveConfigurationIsDefinedIfStateIsActiveValidator implements
        HibernateConstraintValidator<ActiveConfigurationIsDefinedIfStateIsActive, Integration.State> {

    @Override
    public boolean isValid(Integration.State value, HibernateConstraintValidatorContext hibernateConstraintValidatorContext) {
        return value != Integration.State.ACTIVE ||
                hibernateConstraintValidatorContext
                        .getConstraintValidatorPayload(IntegrationValidationContext.class)
                        .getConfiguration() != null;
    }

}
