package no.novari.flyt.integration.model.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
