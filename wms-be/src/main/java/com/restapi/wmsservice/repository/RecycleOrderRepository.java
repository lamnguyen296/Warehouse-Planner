package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.RecycleOrder;
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
public interface RecycleOrderRepository extends JpaRepository<RecycleOrder, Long> {

    @EntityGraph(attributePaths = {"planningDetail", "fromItem", "toItem"})
    Optional<RecycleOrder> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ro FROM RecycleOrder ro WHERE ro.id = :id")
    Optional<RecycleOrder> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"planningDetail", "fromItem", "toItem"})
    List<RecycleOrder> findAll();

    boolean existsByOrderNo(String orderNo);

    boolean existsByPlanningDetailId(Long planningDetailId);

    List<RecycleOrder> findByPlanningDetailId(Long planningDetailId);
}
