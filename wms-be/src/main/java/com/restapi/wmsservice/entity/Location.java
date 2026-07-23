package com.restapi.wmsservice.entity;

import com.restapi.wmsservice.enums.LocationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "location")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Location extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    Warehouse warehouse;

    @Column(nullable = false, length = 50)
    String code;

    @Column(length = 50)
    String zone;

    @Column(length = 50)
    String rack;

    Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    LocationStatus status;
}
