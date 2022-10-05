package no.fintlabs.integration;

import no.fintlabs.integration.model.Integration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntegrationRepository extends JpaRepository<Integration, Long> {

    boolean existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
            Long sourceApplicationId,
            String sourceApplicationIntegrationId
    );

    Optional<Integration> findIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
            Long sourceApplicationId,
            String sourceApplicationIntegrationId
    );

}
