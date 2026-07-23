package com.restapi.wmsservice.entity;

import com.restapi.wmsservice.enums.ItemStatus;
import com.restapi.wmsservice.enums.ItemType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "item")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true, length = 50)
    String code;

    @Column(nullable = false)
    String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 30)
    ItemType itemType;

    @Column(length = 20)
    String unit;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(name = "image_public_id")
    String imagePublicId;

    @Column(name = "image_url", length = 1000)
    String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    ItemStatus status;

    @OneToMany(mappedBy = "parentItem", cascade = CascadeType.ALL, orphanRemoval = true)
    java.util.List<Bom> parentBoms = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "childItem", cascade = CascadeType.ALL, orphanRemoval = true)
    java.util.List<Bom> childBoms = new java.util.ArrayList<>();
}
