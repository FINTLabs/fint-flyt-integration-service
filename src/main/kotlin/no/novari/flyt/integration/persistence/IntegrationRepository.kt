package no.novari.flyt.integration.persistence

import no.novari.flyt.integration.persistence.entity.Integration
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface IntegrationRepository : JpaRepository<Integration, Long> {
    fun existsIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
        sourceApplicationId: Long,
        sourceApplicationIntegrationId: String,
    ): Boolean

    fun findIntegrationBySourceApplicationIdAndSourceApplicationIntegrationId(
        sourceApplicationId: Long,
        sourceApplicationIntegrationId: String,
    ): Integration?

    fun findIntegrationsBySourceApplicationIdIn(sourceApplicationIds: Set<Long>): List<Integration>

    fun findIntegrationsBySourceApplicationIdIn(
        sourceApplicationIds: Set<Long>,
        pageable: Pageable,
    ): Page<Integration>
}
