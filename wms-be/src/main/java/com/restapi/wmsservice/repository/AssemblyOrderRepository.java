package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.AssemblyOrder;
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
public interface AssemblyOrderRepository extends JpaRepository<AssemblyOrder, Long> {

    @EntityGraph(attributePaths = {"planningDetail", "setItem", "components", "components.item"})
    Optional<AssemblyOrder> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"planningDetail", "setItem", "components", "components.item"})
    @Query("SELECT ao FROM AssemblyOrder ao WHERE ao.id = :id")
    Optional<AssemblyOrder> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"planningDetail", "setItem", "components", "components.item"})
    List<AssemblyOrder> findAll();

    boolean existsByAssemblyNo(String assemblyNo);

    boolean existsByPlanningDetailId(Long planningDetailId);

    List<AssemblyOrder> findByPlanningDetailId(Long planningDetailId);
}
