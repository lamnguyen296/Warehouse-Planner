package com.restapi.wmsservice.entity;

import com.restapi.wmsservice.enums.RecycleStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "recycle_order")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecycleOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    String orderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planning_detail_id")
    PlanningDetail planningDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_item_id", nullable = false)
    Item fromItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_item_id", nullable = false)
    Item toItem;

    @Column(nullable = false)
    Integer quantity;

    @Column(name = "expected_yield")
    Integer expectedYield;

    @Column(name = "conversion_ratio", nullable = false)
    Integer conversionRatio;

    @Column(name = "actual_yield")
    Integer actualYield;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    RecycleStatus status;

    @Column(name = "start_time")
    LocalDateTime startTime;

    @Column(name = "finish_time")
    LocalDateTime finishTime;
}
