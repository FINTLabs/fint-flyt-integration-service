package no.fintlabs.integration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SourceApplicationIdAndSourceApplicationIntegrationIdWrapper {
    private Long sourceApplicationId;
    private String sourceApplicationIntegrationId;
}
