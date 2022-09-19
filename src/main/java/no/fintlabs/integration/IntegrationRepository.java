package no.fintlabs.integration;

import no.fintlabs.integration.model.Integration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationRepository extends JpaRepository<Integration, Long> {

    boolean existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
            String sourceApplicationId,
            String sourceApplicationIntegrationId
    );

}
