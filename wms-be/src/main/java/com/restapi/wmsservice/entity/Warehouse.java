package com.restapi.wmsservice.entity;

import com.restapi.wmsservice.enums.WarehouseStatus;
import com.restapi.wmsservice.enums.WarehouseType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Table(name = "warehouse")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Warehouse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true, length = 50)
    String code;

    @Column(nullable = false)
    String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    WarehouseType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    WarehouseStatus status;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<Location> locations;
}
