package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByWarehouseIdAndCode(Long warehouseId, String code);
    boolean existsByWarehouseIdAndCodeAndIdNot(Long warehouseId, String code, Long id);
    boolean existsByWarehouseId(Long warehouseId);
    List<Location> findByWarehouseId(Long warehouseId);
    @EntityGraph(attributePaths = {"warehouse"})
    List<Location> findAll();
}
