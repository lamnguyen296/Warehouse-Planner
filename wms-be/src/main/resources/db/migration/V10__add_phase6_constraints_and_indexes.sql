UPDATE inventory
SET total_quantity = GREATEST(total_quantity, 0),
    reserved_quantity = GREATEST(reserved_quantity, 0);

UPDATE inventory
SET reserved_quantity = LEAST(reserved_quantity, total_quantity),
    available_quantity = total_quantity - LEAST(reserved_quantity, total_quantity);

UPDATE planning_detail pd
LEFT JOIN (
    SELECT planning_detail_id, SUM(quantity) AS reserved_quantity
    FROM inventory_reservation
    WHERE status = 'RESERVED'
    GROUP BY planning_detail_id
) active_reservation ON active_reservation.planning_detail_id = pd.id
SET pd.reserved_quantity = COALESCE(active_reservation.reserved_quantity, 0);

ALTER TABLE inventory
    ADD CONSTRAINT chk_inventory_quantities_non_negative
        CHECK (total_quantity >= 0 AND reserved_quantity >= 0 AND available_quantity >= 0),
    ADD CONSTRAINT chk_inventory_quantity_balance
        CHECK (available_quantity = total_quantity - reserved_quantity);

ALTER TABLE inventory_reservation
    ADD CONSTRAINT chk_inventory_reservation_quantity CHECK (quantity > 0);

ALTER TABLE bom
    ADD CONSTRAINT chk_bom_quantity CHECK (quantity > 0),
    ADD CONSTRAINT chk_bom_parent_child CHECK (parent_item_id <> child_item_id);

ALTER TABLE purchase_request_detail
    ADD CONSTRAINT chk_purchase_detail_quantity CHECK (quantity > 0),
    ADD CONSTRAINT chk_purchase_received_quantity
        CHECK (received_quantity >= 0 AND received_quantity <= quantity);

ALTER TABLE recycle_order
    ADD CONSTRAINT chk_recycle_order_quantity CHECK (quantity > 0),
    ADD CONSTRAINT chk_recycle_actual_yield CHECK (actual_yield IS NULL OR actual_yield >= 0);

ALTER TABLE assembly_order
    ADD CONSTRAINT chk_assembly_order_quantity CHECK (quantity > 0);

ALTER TABLE transfer_order
    ADD CONSTRAINT chk_transfer_order_quantity CHECK (quantity > 0),
    ADD CONSTRAINT chk_transfer_different_warehouses CHECK (from_warehouse_id <> to_warehouse_id);

CREATE INDEX idx_planning_workshop_request ON planning(workshop_request_id);
CREATE INDEX idx_planning_detail_planning_item ON planning_detail(planning_id, item_id);
CREATE INDEX idx_inv_res_planning_detail ON inventory_reservation(planning_detail_id);
CREATE INDEX idx_inv_res_item_warehouse ON inventory_reservation(item_id, warehouse_id);
CREATE INDEX idx_inv_trans_reference ON inventory_transaction(reference_type, reference_id, transaction_type, status);
CREATE INDEX idx_purchase_planning_detail ON purchase_request(planning_detail_id);
CREATE INDEX idx_purchase_status ON purchase_request(status);
CREATE INDEX idx_recycle_planning_detail ON recycle_order(planning_detail_id);
CREATE INDEX idx_recycle_status ON recycle_order(status);
CREATE INDEX idx_assembly_planning_detail ON assembly_order(planning_detail_id);
CREATE INDEX idx_assembly_status ON assembly_order(status);
CREATE INDEX idx_transfer_status ON transfer_order(status);
