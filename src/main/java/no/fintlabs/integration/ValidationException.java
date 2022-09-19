package no.fintlabs.integration;

import lombok.Getter;

import java.util.List;

public class ValidationException extends RuntimeException {

    @Getter
    private final List<ValidatorService.Error> validationErrors;

    public ValidationException(List<ValidatorService.Error> validationErrors) {
        super("Validation error(s): " + validationErrors);
        this.validationErrors = validationErrors;
    }

}
