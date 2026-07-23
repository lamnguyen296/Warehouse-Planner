package com.restapi.wmsservice.entity;

import com.restapi.wmsservice.enums.PlanningAction;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "planning_detail")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlanningDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planning_id", nullable = false)
    Planning planning;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    Item item;

    @Column(name = "required_quantity", nullable = false)
    Integer requiredQuantity = 0;

    @Column(name = "available_quantity", nullable = false)
    Integer availableQuantity = 0;

    @Column(name = "recycle_quantity", nullable = false)
    Integer recycleQuantity = 0;

    @Column(name = "purchase_quantity", nullable = false)
    Integer purchaseQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    Integer reservedQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    PlanningAction action;
}
