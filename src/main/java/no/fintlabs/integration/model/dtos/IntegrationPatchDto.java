package no.fintlabs.integration.model.dtos;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.fintlabs.integration.model.entities.Integration;

import java.util.Optional;

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
