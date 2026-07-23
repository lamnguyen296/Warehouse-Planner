package com.restapi.wmsservice.entity;

import com.restapi.wmsservice.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "transfer_order")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "transfer_no", nullable = false, unique = true, length = 50)
    String transferNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planning_id")
    Planning planning;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_warehouse_id", nullable = false)
    Warehouse fromWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_warehouse_id", nullable = false)
    Warehouse toWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    Item item;

    @Column(nullable = false)
    Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    TransferStatus status;
}
