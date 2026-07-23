package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.Planning;
import com.restapi.wmsservice.enums.PlanningStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanningRepository extends JpaRepository<Planning, Long> {

    @EntityGraph(attributePaths = {"workshopRequest", "details", "details.item"})
    Optional<Planning> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"workshopRequest", "details", "details.item"})
    @Query("SELECT p FROM Planning p WHERE p.id = :id")
    Optional<Planning> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"workshopRequest", "details", "details.item"})
    List<Planning> findAll();

    boolean existsByPlanningNo(String planningNo);

    /** Tất cả Planning của một WorkshopRequest (để tính version và lấy lại). */
    @EntityGraph(attributePaths = {"workshopRequest", "details", "details.item"})
    List<Planning> findByWorkshopRequestId(Long workshopRequestId);

    /** Planning theo requestId và status. */
    @EntityGraph(attributePaths = {"workshopRequest", "details", "details.item"})
    List<Planning> findByWorkshopRequestIdAndStatus(Long workshopRequestId, PlanningStatus status);

    /** Đếm số Planning theo requestId – dùng để sinh version number. */
    long countByWorkshopRequestId(Long workshopRequestId);

    /** Lấy Planning theo status (dùng cho getByStatus query). */
    @EntityGraph(attributePaths = {"workshopRequest", "details", "details.item"})
    List<Planning> findByStatus(PlanningStatus status);
}
