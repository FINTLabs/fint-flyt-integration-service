package no.fintlabs.integration;

import no.fintlabs.integration.model.entities.Integration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IntegrationRepository extends JpaRepository<Integration, Long> {

    boolean existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
            Long sourceApplicationId,
            String sourceApplicationIntegrationId
    );

    Optional<Integration> findIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
            Long sourceApplicationId,
            String sourceApplicationIntegrationId
    );

    List<Integration> findIntegrationsBySourceApplicationIdIn(Set<Long> sourceApplicationIds);

    Page<Integration> findIntegrationsBySourceApplicationIdIn(Set<Long> sourceApplicationIds, Pageable pageable);

}
