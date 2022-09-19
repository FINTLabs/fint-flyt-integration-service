package no.fintlabs.integration;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ValidatorService {

    @Data
    @Builder
    public static class Error {
        private final String fieldPath;
        private final String errorMessage;
    }

    private final Validator validator;

    public ValidatorService(ValidatorFactory validatorFactory) {
        this.validator = validatorFactory.getValidator();
    }

    public Optional<List<Error>> validate(Object object) {
        List<Error> errors = validator.validate(object)
                .stream()
                .map(constraintViolation -> Error
                        .builder()
                        .fieldPath(constraintViolation.getPropertyPath().toString())
                        .errorMessage(constraintViolation.getMessage())
                        .build()
                )
                .sorted(Comparator.comparing(Error::getFieldPath))
                .toList();

        return errors.isEmpty()
                ? Optional.empty()
                : Optional.of(errors);
    }

}
