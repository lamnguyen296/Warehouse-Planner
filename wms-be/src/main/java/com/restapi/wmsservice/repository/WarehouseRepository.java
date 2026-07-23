package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.Warehouse;
import com.restapi.wmsservice.enums.WarehouseType;
import com.restapi.wmsservice.enums.WarehouseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByCode(String code);
    boolean existsByCode(String code);

    /** Tìm tất cả warehouse theo type (COMPONENT_WAREHOUSE, SET_WAREHOUSE, WORKSHOP). */
    List<Warehouse> findByType(WarehouseType type);

    List<Warehouse> findByTypeAndStatus(WarehouseType type, WarehouseStatus status);

    boolean existsByTypeAndStatus(WarehouseType type, WarehouseStatus status);

    boolean existsByTypeAndStatusAndIdNot(WarehouseType type, WarehouseStatus status, Long id);
}
