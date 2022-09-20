package no.fintlabs.integration;

import no.fintlabs.integration.model.Integration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntegrationRepository extends JpaRepository<Integration, Long> {

    boolean existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
            String sourceApplicationId,
            String sourceApplicationIntegrationId
    );

    Optional<Integration> findIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
            String sourceApplicationId,
            String sourceApplicationIntegrationId
    );

}
