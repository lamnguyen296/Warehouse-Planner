package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.PlanningDetail;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanningDetailRepository extends JpaRepository<PlanningDetail, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pd FROM PlanningDetail pd WHERE pd.id = :id")
    Optional<PlanningDetail> findByIdForUpdate(@Param("id") Long id);

    List<PlanningDetail> findByPlanningIdAndItemId(Long planningId, Long itemId);
}
