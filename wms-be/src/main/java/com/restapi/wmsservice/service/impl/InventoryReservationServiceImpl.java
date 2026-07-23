package com.restapi.wmsservice.service.impl;

import org.springframework.transaction.annotation.Transactional;
import com.restapi.wmsservice.dto.request.InventoryReservationRequest;
import com.restapi.wmsservice.dto.response.InventoryReservationResponse;
import com.restapi.wmsservice.entity.Inventory;
import com.restapi.wmsservice.entity.InventoryReservation;
import com.restapi.wmsservice.entity.Item;
import com.restapi.wmsservice.entity.PlanningDetail;
import com.restapi.wmsservice.entity.Warehouse;
import com.restapi.wmsservice.enums.ReservationStatus;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.InventoryReservationMapper;
import com.restapi.wmsservice.repository.InventoryRepository;
import com.restapi.wmsservice.repository.InventoryReservationRepository;
import com.restapi.wmsservice.repository.ItemRepository;
import com.restapi.wmsservice.repository.PlanningDetailRepository;
import com.restapi.wmsservice.repository.WarehouseRepository;
import com.restapi.wmsservice.service.InventoryReservationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryReservationServiceImpl implements InventoryReservationService {

    InventoryReservationRepository reservationRepository;
    InventoryReservationMapper reservationMapper;
    ItemRepository itemRepository;
    WarehouseRepository warehouseRepository;
    PlanningDetailRepository planningDetailRepository;
    InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public InventoryReservationResponse createReservation(InventoryReservationRequest request) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
        PlanningDetail planningDetail = planningDetailRepository.findByIdForUpdate(request.getPlanningDetailId())
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));
        validatePlanningItem(planningDetail, item);

        InventoryReservation reservation = reservationMapper.toReservation(request);
        reservation.setPlanningDetail(planningDetail);
        reservation.setItem(item);
        reservation.setWarehouse(warehouse);
        if (reservation.getStatus() == ReservationStatus.RESERVED) {
            Inventory inventory = reserveInventoryRecord(item.getId(), warehouse.getId(), reservation.getQuantity());
            reservation.setInventory(inventory);
            increasePlanningReservedQuantity(planningDetail, reservation.getQuantity());
        }

        return reservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Override
    public InventoryReservationResponse getReservation(Long id) {
        return reservationRepository.findById(id)
                .map(reservationMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    @Override
    public List<InventoryReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public InventoryReservationResponse updateReservation(Long id, InventoryReservationRequest request) {
        InventoryReservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
        PlanningDetail planningDetail = planningDetailRepository.findByIdForUpdate(request.getPlanningDetailId())
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));
        validatePlanningItem(planningDetail, item);

        if (reservation.getStatus() == ReservationStatus.RESERVED && reservation.getInventory() != null) {
            releaseInventoryRecord(reservation.getInventory(), reservation.getQuantity());
            decreasePlanningReservedQuantity(reservation.getPlanningDetail(), reservation.getQuantity());
        }

        reservationMapper.updateReservation(reservation, request);
        reservation.setPlanningDetail(planningDetail);
        reservation.setItem(item);
        reservation.setWarehouse(warehouse);
        reservation.setInventory(null);
        if (reservation.getStatus() == ReservationStatus.RESERVED) {
            Inventory inventory = reserveInventoryRecord(item.getId(), warehouse.getId(), reservation.getQuantity());
            reservation.setInventory(inventory);
            increasePlanningReservedQuantity(planningDetail, reservation.getQuantity());
        }

        return reservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public void deleteReservation(Long id) {
        InventoryReservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND));
        if (reservation.getStatus() == ReservationStatus.RESERVED && reservation.getInventory() != null) {
            releaseInventoryRecord(reservation.getInventory(), reservation.getQuantity());
            PlanningDetail planningDetail = planningDetailRepository
                    .findByIdForUpdate(reservation.getPlanningDetail().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));
            decreasePlanningReservedQuantity(planningDetail, reservation.getQuantity());
        }
        reservationRepository.delete(reservation);
    }

    private Inventory reserveInventoryRecord(Long itemId, Long warehouseId, int quantity) {
        Inventory inventory = inventoryRepository.findByItemIdAndWarehouseId(itemId, warehouseId).stream()
                .filter(inv -> inv.getAvailableQuantity() >= quantity)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INSUFFICIENT_AVAILABLE_STOCK));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        return inventoryRepository.save(inventory);
    }

    private void releaseInventoryRecord(Inventory inventory, int quantity) {
        if (inventory.getReservedQuantity() < quantity) {
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory.setAvailableQuantity(inventory.getTotalQuantity() - inventory.getReservedQuantity());
        inventoryRepository.save(inventory);
    }

    private void validatePlanningItem(PlanningDetail planningDetail, Item item) {
        if (!planningDetail.getItem().getId().equals(item.getId())) {
            throw new AppException(ErrorCode.RESERVATION_ITEM_MISMATCH);
        }
    }

    private void increasePlanningReservedQuantity(PlanningDetail planningDetail, int quantity) {
        planningDetail.setReservedQuantity(planningDetail.getReservedQuantity() + quantity);
        planningDetailRepository.save(planningDetail);
    }

    private void decreasePlanningReservedQuantity(PlanningDetail planningDetail, int quantity) {
        planningDetail.setReservedQuantity(Math.max(0, planningDetail.getReservedQuantity() - quantity));
        planningDetailRepository.save(planningDetail);
    }
}
