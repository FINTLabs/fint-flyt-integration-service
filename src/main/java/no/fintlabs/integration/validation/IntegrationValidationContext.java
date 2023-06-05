package no.fintlabs.integration.validation;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.integration.model.dtos.ConfigurationDto;

import javax.validation.Payload;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class IntegrationValidationContext implements Payload {
    private final Long integrationId;
    private final ConfigurationDto configuration;
}
