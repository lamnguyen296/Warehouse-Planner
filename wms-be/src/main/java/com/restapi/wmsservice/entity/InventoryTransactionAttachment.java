package com.restapi.wmsservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "inventory_transaction_attachment")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransactionAttachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    InventoryTransaction transaction;

    @Column(name = "public_id", nullable = false, unique = true)
    String publicId;

    @Column(name = "secure_url", nullable = false, length = 1000)
    String secureUrl;

    @Column(name = "original_filename", nullable = false)
    String originalFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    String contentType;

    @Column(name = "file_size", nullable = false)
    Long fileSize;

    @Column(name = "uploaded_by", nullable = false)
    String uploadedBy;
}
