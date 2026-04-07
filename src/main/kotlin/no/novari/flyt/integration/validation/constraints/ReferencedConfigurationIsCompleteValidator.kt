package no.novari.flyt.integration.validation.constraints

import no.novari.flyt.integration.validation.IntegrationValidationContext
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext

class ReferencedConfigurationIsCompleteValidator :
    HibernateConstraintValidator<ReferencedConfigurationIsComplete, Long> {
    override fun isValid(
        value: Long?,
        hibernateConstraintValidatorContext: HibernateConstraintValidatorContext,
    ): Boolean {
        val integrationValidationContext =
            hibernateConstraintValidatorContext.getConstraintValidatorPayload(
                IntegrationValidationContext::class.java,
            )

        return value == null || integrationValidationContext?.configuration?.completed == true
    }
}
