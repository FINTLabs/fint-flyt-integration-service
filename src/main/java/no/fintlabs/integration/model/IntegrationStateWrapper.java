package no.fintlabs.integration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationStateWrapper {
    @NotNull
    private IntegrationState state;
}
