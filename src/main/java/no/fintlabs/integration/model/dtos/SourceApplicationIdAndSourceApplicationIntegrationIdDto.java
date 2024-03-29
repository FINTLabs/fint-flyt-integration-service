package no.fintlabs.integration.model.dtos;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SourceApplicationIdAndSourceApplicationIntegrationIdDto {
    private Long sourceApplicationId;
    private String sourceApplicationIntegrationId;
}
