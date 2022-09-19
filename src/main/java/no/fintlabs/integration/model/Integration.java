package no.fintlabs.integration.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "UniqueSourceApplicationIdAndSourceApplicationIntegrationId",
                columnNames = {"sourceApplicationId", "sourceApplicationIntegrationId"}
        )
})
public class Integration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private long id;

    @NotBlank
    private String sourceApplicationId;

    @NotBlank
    private String sourceApplicationIntegrationId;

    @NotBlank
    private String destination;

    @NotNull
    @Enumerated(EnumType.STRING)
    private IntegrationState state;

    private String activeConfigurationId;

}
