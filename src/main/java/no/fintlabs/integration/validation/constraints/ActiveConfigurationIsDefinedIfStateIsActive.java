package no.fintlabs.integration.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = ActiveConfigurationIsDefinedIfStateIsActiveValidator.class)
public @interface ActiveConfigurationIsDefinedIfStateIsActive {

    String message() default "is cannot be set to ACTIVE without an active configuration";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
