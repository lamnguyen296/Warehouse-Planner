package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.WorkshopRequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkshopRequestDetailRepository extends JpaRepository<WorkshopRequestDetail, Long> {
}
