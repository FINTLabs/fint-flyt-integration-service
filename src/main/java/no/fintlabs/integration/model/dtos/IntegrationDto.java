package no.fintlabs.integration.model.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fintlabs.integration.model.entities.Integration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntegrationDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long id;

    @NotNull
    private Long sourceApplicationId;

    @NotBlank
    private String sourceApplicationIntegrationId;

    @NotBlank
    private String destination;

    @NotNull
    private Integration.State state;

    private Long activeConfigurationId;

}
