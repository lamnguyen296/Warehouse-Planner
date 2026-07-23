package com.restapi.wmsservice.entity;

import com.restapi.wmsservice.enums.PurchaseStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_request")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "request_no", nullable = false, unique = true, length = 50)
    String requestNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planning_detail_id")
    PlanningDetail planningDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    PurchaseStatus status;

    @OneToMany(mappedBy = "purchaseRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PurchaseRequestDetail> details = new ArrayList<>();
}
