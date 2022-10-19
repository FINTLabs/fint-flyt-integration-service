package no.fintlabs.integration.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SourceApplicationIdAndSourceApplicationIntegrationIdDto {
    private Long sourceApplicationId;
    private String sourceApplicationIntegrationId;
}
