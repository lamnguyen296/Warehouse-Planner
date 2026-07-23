package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.Bom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

@Repository
public interface BomRepository extends JpaRepository<Bom, Long> {
    @EntityGraph(attributePaths = {"parentItem", "childItem"})
    List<Bom> findByParentItemId(Long parentItemId);
    
    @EntityGraph(attributePaths = {"parentItem", "childItem"})
    List<Bom> findByChildItemId(Long childItemId);
    
    Optional<Bom> findByParentItemIdAndChildItemId(Long parentItemId, Long childItemId);
    boolean existsByParentItemIdAndChildItemId(Long parentItemId, Long childItemId);

    @org.springframework.data.jpa.repository.Query(value = """
        WITH RECURSIVE bom_cte (id, parent_item_id, child_item_id, quantity, priority, created_at, updated_at, path, depth) AS (
            SELECT id, parent_item_id, child_item_id, quantity, priority, created_at, updated_at,
                   CAST(CONCAT(parent_item_id, ',', child_item_id) AS CHAR(1000)) AS path,
                   1 AS depth
            FROM bom
            WHERE parent_item_id = :parentItemId
            UNION ALL
            SELECT b.id, b.parent_item_id, b.child_item_id, (b.quantity * c.quantity) as quantity, b.priority, b.created_at, b.updated_at,
                   CONCAT(c.path, ',', b.child_item_id) AS path,
                   c.depth + 1 AS depth
            FROM bom b
            INNER JOIN bom_cte c ON b.parent_item_id = c.child_item_id
            WHERE FIND_IN_SET(b.child_item_id, c.path) = 0
              AND c.depth < 50
        )
        SELECT id, parent_item_id, child_item_id, quantity, priority, created_at, updated_at FROM bom_cte
        """, nativeQuery = true)
    List<Bom> findBomHierarchy(@org.springframework.data.repository.query.Param("parentItemId") Long parentItemId);
    
    @EntityGraph(attributePaths = {"parentItem", "childItem"})
    List<Bom> findAll();
}
