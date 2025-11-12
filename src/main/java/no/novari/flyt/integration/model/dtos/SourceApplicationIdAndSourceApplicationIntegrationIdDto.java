package no.novari.flyt.integration.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
