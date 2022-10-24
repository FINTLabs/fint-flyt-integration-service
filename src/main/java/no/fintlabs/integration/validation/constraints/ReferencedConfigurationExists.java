package no.fintlabs.integration.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = ReferencedConfigurationExistsValidator.class)
public @interface ReferencedConfigurationExists {

    String message() default "referenced configuration could not be found";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
