ALTER TABLE inventory_reservation
    ADD COLUMN inventory_id BIGINT NULL,
    ADD CONSTRAINT fk_inv_res_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(id);

CREATE INDEX idx_inv_res_inventory ON inventory_reservation(inventory_id);
