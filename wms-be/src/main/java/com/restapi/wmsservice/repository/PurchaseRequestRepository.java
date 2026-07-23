package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.PurchaseRequest;
import com.restapi.wmsservice.enums.PurchaseStatus;
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
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {

    @EntityGraph(attributePaths = {"planningDetail", "details", "details.item"})
    Optional<PurchaseRequest> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pr FROM PurchaseRequest pr WHERE pr.id = :id")
    Optional<PurchaseRequest> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"planningDetail", "details", "details.item"})
    List<PurchaseRequest> findAll();

    @EntityGraph(attributePaths = {"planningDetail", "details", "details.item"})
    List<PurchaseRequest> findByStatus(PurchaseStatus status);

    boolean existsByRequestNo(String requestNo);

    boolean existsByPlanningDetailId(Long planningDetailId);

    @EntityGraph(attributePaths = {"planningDetail", "details", "details.item"})
    List<PurchaseRequest> findByPlanningDetailId(Long planningDetailId);
}
