package no.fintlabs.integration.model.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "UniqueSourceApplicationIdAndSourceApplicationIntegrationId",
                columnNames = {"sourceApplicationId", "sourceApplicationIntegrationId"}
        )
})
public class Integration {

    public enum State {
        ACTIVE,
        DEACTIVATED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private long id;

    @NotNull
    private Long sourceApplicationId;

    @NotBlank
    private String sourceApplicationIntegrationId;

    @NotBlank
    private String destination;

    @NotNull
    @Enumerated(EnumType.STRING)
    private State state;

    private Long activeConfigurationId;

}
