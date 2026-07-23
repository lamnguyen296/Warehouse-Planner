package com.restapi.wmsservice.entity;

import com.restapi.wmsservice.enums.TransactionStatus;
import com.restapi.wmsservice.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory_transaction")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "transaction_no", nullable = false, unique = true, length = 50)
    String transactionNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    TransactionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_warehouse_id")
    Warehouse fromWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_warehouse_id")
    Warehouse toWarehouse;

    @Column(name = "reference_type", length = 50)
    String referenceType;

    @Column(name = "reference_id")
    Long referenceId;

    @Column(name = "created_by", length = 50)
    String createdBy;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    List<InventoryTransactionDetail> details = new ArrayList<>();
}
