package com.restapi.wmsservice.entity;

import com.restapi.wmsservice.enums.RequestPriority;
import com.restapi.wmsservice.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workshop_request")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkshopRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "request_no", nullable = false, unique = true, length = 50)
    String requestNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    RequestPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    RequestStatus status;

    @Column(name = "requested_date")
    LocalDateTime requestedDate;

    @Column(name = "expected_date")
    LocalDateTime expectedDate;

    @Column(name = "created_by", length = 50)
    String createdBy;

    @OneToMany(mappedBy = "workshopRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    List<WorkshopRequestDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "workshopRequest")
    List<Planning> plannings = new ArrayList<>();
}
