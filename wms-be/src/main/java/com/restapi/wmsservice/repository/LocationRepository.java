package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.List;
import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByCode(String code);
    boolean existsByCode(String code);
    List<Location> findByWarehouseId(Long warehouseId);
    @EntityGraph(attributePaths = {"warehouse"})
    List<Location> findAll();
}
