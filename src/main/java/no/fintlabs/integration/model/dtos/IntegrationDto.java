package no.fintlabs.integration.model.dtos;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.integration.model.entities.Integration;
import no.fintlabs.integration.validation.constraints.ActiveConfigurationIsDefinedIfStateIsActive;
import no.fintlabs.integration.validation.constraints.ReferencedConfigurationExists;
import no.fintlabs.integration.validation.constraints.ReferencedConfigurationIsComplete;
import no.fintlabs.integration.validation.constraints.ReferencedConfigurationIsForIntegration;
import no.fintlabs.integration.validation.groups.ActiveConfigurationIsCompleteGroup;
import no.fintlabs.integration.validation.groups.ActiveConfigurationIsForIntegrationGroup;

import javax.validation.GroupSequence;

@GroupSequence({IntegrationDto.class, ActiveConfigurationIsForIntegrationGroup.class, ActiveConfigurationIsCompleteGroup.class})
@Getter
@EqualsAndHashCode
@Jacksonized
@Builder(toBuilder = true)

public class IntegrationDto {

    private long id;

    private Long sourceApplicationId;

    private String sourceApplicationIntegrationId;

    private String destination;

    @ActiveConfigurationIsDefinedIfStateIsActive
    private Integration.State state;

    @ReferencedConfigurationExists
    @ReferencedConfigurationIsForIntegration(groups = ActiveConfigurationIsForIntegrationGroup.class)
    @ReferencedConfigurationIsComplete(groups = ActiveConfigurationIsCompleteGroup.class)
    private Long activeConfigurationId;

}
