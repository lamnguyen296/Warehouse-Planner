package com.restapi.wmsservice.entity;

import com.restapi.wmsservice.enums.PlanningStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "planning")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Planning extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "planning_no", nullable = false, unique = true, length = 50)
    String planningNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_request_id", nullable = false)
    WorkshopRequest workshopRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    PlanningStatus status;

    @OneToMany(mappedBy = "planning", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PlanningDetail> details = new ArrayList<>();
}
