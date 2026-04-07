package no.novari.flyt.integration.validation.constraints

import no.novari.flyt.integration.persistence.entity.Integration
import no.novari.flyt.integration.validation.IntegrationValidationContext
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext

class ActiveConfigurationIsDefinedIfStateIsActiveValidator :
    HibernateConstraintValidator<ActiveConfigurationIsDefinedIfStateIsActive, Integration.State> {
    override fun isValid(
        value: Integration.State?,
        hibernateConstraintValidatorContext: HibernateConstraintValidatorContext,
    ): Boolean {
        return value != Integration.State.ACTIVE ||
            hibernateConstraintValidatorContext
                .getConstraintValidatorPayload(IntegrationValidationContext::class.java)
                ?.configuration != null
    }
}
