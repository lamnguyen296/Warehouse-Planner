package com.restapi.wmsservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "bom")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Bom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_item_id", nullable = false)
    Item parentItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_item_id", nullable = false)
    Item childItem;

    @Column(nullable = false)
    Integer quantity;

    @Column(nullable = false)
    Integer priority;
}
