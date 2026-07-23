package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.Inventory;
import com.restapi.wmsservice.enums.WarehouseType;
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
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @EntityGraph(attributePaths = {"warehouse", "location", "item"})
    Optional<Inventory> findByWarehouseIdAndLocationIdAndItemId(Long warehouseId, Long locationId, Long itemId);

    @EntityGraph(attributePaths = {"warehouse", "location", "item"})
    Optional<Inventory> findById(Long id);

    @EntityGraph(attributePaths = {"warehouse", "location", "item"})
    List<Inventory> findAll();

    /**
     * Tổng available_quantity của một item trên tất cả locations thuộc warehouse type chỉ định.
     * Dùng bởi Planning Engine để kiểm tra tồn kho khả dụng.
     */
    @Query("SELECT COALESCE(SUM(i.availableQuantity), 0) FROM Inventory i " +
           "WHERE i.item.id = :itemId AND i.warehouse.type = :warehouseType " +
           "AND i.warehouse.status = com.restapi.wmsservice.enums.WarehouseStatus.ACTIVE")
    int sumAvailableQuantityByItemAndWarehouseType(
            @Param("itemId") Long itemId,
            @Param("warehouseType") WarehouseType warehouseType);

    /**
     * Tổng available_quantity của một item trên nhiều warehouse types.
     * Dùng để kiểm tra tổng tồn kho (SET + COMPONENT warehouse).
     */
    @Query("SELECT COALESCE(SUM(i.availableQuantity), 0) FROM Inventory i " +
           "WHERE i.item.id = :itemId")
    int sumAvailableQuantityByItem(@Param("itemId") Long itemId);

    /**
     * Lấy tất cả inventory records của một item (dùng trong Planning để update reserved).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findByItemId(Long itemId);

    /**
     * Tìm inventory của item trên warehouse cụ thể (tất cả locations trong warehouse đó).
     * Dùng trong Reserve/Deduct để chọn đúng warehouse.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findByItemIdAndWarehouseId(Long itemId, Long warehouseId);

    /**
     * Tìm inventory theo item + warehouse + location (unique key).
     * Dùng để upsert khi nhận hàng (receiveGoods).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findByItemIdAndWarehouseIdAndLocationId(Long itemId, Long warehouseId, Long locationId);
}
