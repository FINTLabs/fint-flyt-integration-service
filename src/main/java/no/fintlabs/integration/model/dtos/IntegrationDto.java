package no.fintlabs.integration.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fintlabs.integration.model.entities.Integration;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntegrationDto {

    private long id;

    private Long sourceApplicationId;

    private String sourceApplicationIntegrationId;

    private String destination;

    private Integration.State state;

    private Long activeConfigurationId;

}