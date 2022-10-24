package no.fintlabs.integration.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = ReferencedConfigurationIsForIntegrationValidator.class)
public @interface ReferencedConfigurationIsForIntegration {

    String message() default "referenced configuration is not for this integration";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
