CREATE TABLE assembly_order_component (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assembly_order_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    required_quantity INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_assembly_component_order
        FOREIGN KEY (assembly_order_id) REFERENCES assembly_order(id) ON DELETE CASCADE,
    CONSTRAINT fk_assembly_component_item
        FOREIGN KEY (item_id) REFERENCES item(id),
    CONSTRAINT uq_assembly_component_item UNIQUE (assembly_order_id, item_id),
    CONSTRAINT chk_assembly_component_quantity CHECK (required_quantity > 0)
);

ALTER TABLE recycle_order
    ADD COLUMN conversion_ratio INT NULL AFTER expected_yield;

UPDATE recycle_order ro
JOIN bom b ON b.parent_item_id = ro.from_item_id AND b.child_item_id = ro.to_item_id
SET ro.conversion_ratio = b.quantity
WHERE ro.conversion_ratio IS NULL;

UPDATE recycle_order
SET conversion_ratio = 1
WHERE conversion_ratio IS NULL;

ALTER TABLE recycle_order
    MODIFY COLUMN conversion_ratio INT NOT NULL,
    ADD CONSTRAINT chk_recycle_conversion_ratio CHECK (conversion_ratio > 0);

ALTER TABLE inventory_reservation
    MODIFY COLUMN planning_detail_id BIGINT NULL,
    ADD COLUMN recycle_order_id BIGINT NULL AFTER planning_detail_id,
    ADD CONSTRAINT fk_inv_res_recycle_order
        FOREIGN KEY (recycle_order_id) REFERENCES recycle_order(id);

ALTER TABLE inventory_reservation
    ADD CONSTRAINT chk_inv_res_business_reference
        CHECK (planning_detail_id IS NOT NULL OR recycle_order_id IS NOT NULL);

CREATE INDEX idx_inv_res_recycle_status
    ON inventory_reservation(recycle_order_id, status);

UPDATE inventory_reservation r
JOIN planning_detail pd ON pd.id = r.planning_detail_id
JOIN planning p ON p.id = pd.planning_id
SET r.expired_time = NULL
WHERE r.status = 'RESERVED' AND p.status = 'EXECUTING';
