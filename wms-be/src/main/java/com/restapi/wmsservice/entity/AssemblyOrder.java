package com.restapi.wmsservice.entity;

import com.restapi.wmsservice.enums.AssemblyStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "assembly_order")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssemblyOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "assembly_no", nullable = false, unique = true, length = 50)
    String assemblyNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planning_detail_id")
    PlanningDetail planningDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_item_id", nullable = false)
    Item setItem;

    @Column(nullable = false)
    Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    AssemblyStatus status;
}
