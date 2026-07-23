package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.WorkshopRequest;
import com.restapi.wmsservice.enums.RequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkshopRequestRepository extends JpaRepository<WorkshopRequest, Long> {

    @EntityGraph(attributePaths = {"details", "details.item"})
    Optional<WorkshopRequest> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT wr FROM WorkshopRequest wr WHERE wr.id = :id")
    Optional<WorkshopRequest> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"details", "details.item"})
    List<WorkshopRequest> findAll();

    boolean existsByRequestNo(String requestNo);

    @EntityGraph(attributePaths = {"details", "details.item"})
    List<WorkshopRequest> findByStatus(RequestStatus status);

    @EntityGraph(attributePaths = {"details", "details.item"})
    List<WorkshopRequest> findByCreatedBy(String createdBy);

    // Count pending plannings: dùng để check trước khi cancel
    @Query("SELECT COUNT(p) FROM Planning p WHERE p.workshopRequest.id = :requestId " +
           "AND p.status NOT IN (com.restapi.wmsservice.enums.PlanningStatus.COMPLETED, " +
           "com.restapi.wmsservice.enums.PlanningStatus.FAILED)")
    long countActivePlanningsByRequestId(@Param("requestId") Long requestId);
}
