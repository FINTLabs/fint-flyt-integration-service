package no.fintlabs.integration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActiveConfigurationIdWrapper {
    @NotBlank
    private String activeConfigurationId;
}
