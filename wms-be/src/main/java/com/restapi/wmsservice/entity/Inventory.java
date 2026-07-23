package com.restapi.wmsservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "inventory", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"warehouse_id", "location_id", "item_id"})
})
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    Item item;

    @Column(name = "total_quantity", nullable = false)
    Integer totalQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    Integer reservedQuantity = 0;

    @Column(name = "available_quantity", nullable = false)
    Integer availableQuantity = 0;

    @Version
    @Column(nullable = false)
    Long version = 0L;
}
