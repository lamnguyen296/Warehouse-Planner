package com.restapi.wmsservice.entity;

import com.restapi.wmsservice.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_reservation")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryReservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planning_detail_id", nullable = false)
    PlanningDetail planningDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
    Inventory inventory;

    @Column(nullable = false)
    Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    ReservationStatus status;

    @Column(name = "expired_time")
    LocalDateTime expiredTime;
}
