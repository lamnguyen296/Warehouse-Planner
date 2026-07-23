package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.BatchInfo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface BatchInfoRepository extends JpaRepository<BatchInfo, Long> {

    @EntityGraph(attributePaths = {"item"})
    Optional<BatchInfo> findByItemIdAndBatchNo(Long itemId, String batchNo);

    @EntityGraph(attributePaths = {"item"})
    Optional<BatchInfo> findById(Long id);

    @EntityGraph(attributePaths = {"item"})
    List<BatchInfo> findAll();
}
