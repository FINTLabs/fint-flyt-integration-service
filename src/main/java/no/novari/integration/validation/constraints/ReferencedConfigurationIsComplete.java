package no.novari.integration.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = ReferencedConfigurationIsCompleteValidator.class)
public @interface ReferencedConfigurationIsComplete {

    String message() default "referenced configuration is not complete";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
