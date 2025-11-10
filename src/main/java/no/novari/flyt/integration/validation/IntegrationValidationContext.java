package no.novari.flyt.integration.validation;

import jakarta.validation.Payload;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.novari.flyt.integration.model.dtos.ConfigurationDto;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class IntegrationValidationContext implements Payload {
    private final Long integrationId;
    private final ConfigurationDto configuration;
}
