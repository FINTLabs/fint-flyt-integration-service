package no.novari.integration.model.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class IntegrationPostDto {

    @NotNull
    private Long sourceApplicationId;

    @NotBlank
    private String sourceApplicationIntegrationId;

    @NotBlank
    private String destination;

}
