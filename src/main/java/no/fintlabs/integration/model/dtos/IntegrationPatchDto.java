package no.fintlabs.integration.model.dtos;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.fintlabs.integration.model.entities.Integration;
import no.fintlabs.integration.validation.constraints.ActiveConfigurationExists;
import no.fintlabs.integration.validation.constraints.ActiveConfigurationIsComplete;
import no.fintlabs.integration.validation.constraints.ActiveConfigurationIsForIntegration;
import no.fintlabs.integration.validation.groups.ActiveConfigurationIsCompleteGroup;
import no.fintlabs.integration.validation.groups.ActiveConfigurationIsForIntegrationGroup;

import javax.validation.GroupSequence;
import java.util.Optional;

@ActiveConfigurationExists
@ActiveConfigurationIsForIntegration(groups = ActiveConfigurationIsForIntegrationGroup.class)
@ActiveConfigurationIsComplete(groups = ActiveConfigurationIsCompleteGroup.class)
@GroupSequence({IntegrationPatchDto.class, ActiveConfigurationIsForIntegrationGroup.class, ActiveConfigurationIsCompleteGroup.class})
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationPatchDto {

    private String destination;

    private Integration.State state;

    private Long activeConfigurationId;

    public Optional<String> getDestination() {
        return Optional.ofNullable(destination);
    }

    public Optional<Integration.State> getState() {
        return Optional.ofNullable(state);
    }

    public Optional<Long> getActiveConfigurationId() {
        return Optional.ofNullable(activeConfigurationId);
    }

}
