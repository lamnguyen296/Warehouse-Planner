package com.restapi.wmsservice.entity;

import com.restapi.wmsservice.enums.BatchStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_info", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"item_id", "batch_no"})
})
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BatchInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    Item item;

    @Column(name = "batch_no", nullable = false, length = 50)
    String batchNo;

    @Column(name = "manufacture_date")
    LocalDateTime manufactureDate;

    @Column(name = "expired_date")
    LocalDateTime expiredDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    BatchStatus status;
}
