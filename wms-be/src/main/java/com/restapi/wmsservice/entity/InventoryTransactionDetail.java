package com.restapi.wmsservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "inventory_transaction_detail")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransactionDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    InventoryTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    BatchInfo batchInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_from")
    Location locationFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_to")
    Location locationTo;

    @Column(nullable = false)
    Integer quantity;
}
