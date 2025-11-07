package no.novari.integration.model.dtos;

import jakarta.validation.GroupSequence;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.novari.integration.model.entities.Integration;
import no.novari.integration.validation.constraints.ActiveConfigurationIsDefinedIfStateIsActive;
import no.novari.integration.validation.constraints.ReferencedConfigurationExists;
import no.novari.integration.validation.constraints.ReferencedConfigurationIsComplete;
import no.novari.integration.validation.constraints.ReferencedConfigurationIsForIntegration;
import no.novari.integration.validation.groups.ActiveConfigurationIsCompleteGroup;
import no.novari.integration.validation.groups.ActiveConfigurationIsForIntegrationGroup;

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
