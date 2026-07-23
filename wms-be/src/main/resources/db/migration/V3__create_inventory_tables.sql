CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    total_quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    available_quantity INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_inventory_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse(id),
    CONSTRAINT fk_inventory_location FOREIGN KEY (location_id) REFERENCES location(id),
    CONSTRAINT fk_inventory_item FOREIGN KEY (item_id) REFERENCES item(id),
    CONSTRAINT uq_inventory_wh_loc_item UNIQUE (warehouse_id, location_id, item_id)
);

CREATE TABLE batch_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    batch_no VARCHAR(50) NOT NULL,
    manufacture_date DATETIME,
    expired_date DATETIME,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_batch_item FOREIGN KEY (item_id) REFERENCES item(id),
    CONSTRAINT uq_batch_item_batchno UNIQUE (item_id, batch_no)
);

CREATE TABLE inventory_batch_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_ibs_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(id),
    CONSTRAINT fk_ibs_batch FOREIGN KEY (batch_id) REFERENCES batch_info(id),
    CONSTRAINT uq_ibs_inventory_batch UNIQUE (inventory_id, batch_id)
);

CREATE TABLE inventory_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_no VARCHAR(50) NOT NULL UNIQUE,
    transaction_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    from_warehouse_id BIGINT,
    to_warehouse_id BIGINT,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    created_by VARCHAR(50),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_inv_trans_from_wh FOREIGN KEY (from_warehouse_id) REFERENCES warehouse(id),
    CONSTRAINT fk_inv_trans_to_wh FOREIGN KEY (to_warehouse_id) REFERENCES warehouse(id)
);

CREATE TABLE inventory_transaction_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    batch_id BIGINT,
    location_from BIGINT,
    location_to BIGINT,
    quantity INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_itd_transaction FOREIGN KEY (transaction_id) REFERENCES inventory_transaction(id),
    CONSTRAINT fk_itd_item FOREIGN KEY (item_id) REFERENCES item(id),
    CONSTRAINT fk_itd_batch FOREIGN KEY (batch_id) REFERENCES batch_info(id),
    CONSTRAINT fk_itd_loc_from FOREIGN KEY (location_from) REFERENCES location(id),
    CONSTRAINT fk_itd_loc_to FOREIGN KEY (location_to) REFERENCES location(id)
);

CREATE TABLE inventory_reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    planning_detail_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    expired_time DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_inv_res_item FOREIGN KEY (item_id) REFERENCES item(id),
    CONSTRAINT fk_inv_res_wh FOREIGN KEY (warehouse_id) REFERENCES warehouse(id)
);

CREATE INDEX idx_inventory_item ON inventory(item_id);
CREATE INDEX idx_batch_status ON batch_info(status);
CREATE INDEX idx_inv_trans_type ON inventory_transaction(transaction_type);
CREATE INDEX idx_inv_res_status ON inventory_reservation(status);
