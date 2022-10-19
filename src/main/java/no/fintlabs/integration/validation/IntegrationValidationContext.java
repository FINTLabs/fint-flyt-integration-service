package no.fintlabs.integration.validation;

import lombok.Builder;
import lombok.Data;
import no.fintlabs.integration.model.dtos.ConfigurationDto;

import javax.validation.Payload;

@Data
@Builder
public class IntegrationValidationContext implements Payload {
    private final Long integrationId;
    private final ConfigurationDto configuration;
}
