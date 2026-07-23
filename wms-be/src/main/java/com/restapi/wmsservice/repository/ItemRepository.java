package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByCode(String code);
    boolean existsByCode(String code);
}
