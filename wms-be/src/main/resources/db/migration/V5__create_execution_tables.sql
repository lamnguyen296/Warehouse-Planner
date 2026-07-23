CREATE TABLE purchase_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_no VARCHAR(50) NOT NULL UNIQUE,
    planning_detail_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_purchase_req_planning_detail FOREIGN KEY (planning_detail_id) REFERENCES planning_detail(id)
);

CREATE TABLE purchase_request_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_request_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    received_quantity INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_purchase_req_detail_req FOREIGN KEY (purchase_request_id) REFERENCES purchase_request(id),
    CONSTRAINT fk_purchase_req_detail_item FOREIGN KEY (item_id) REFERENCES item(id)
);

CREATE TABLE recycle_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    planning_detail_id BIGINT NOT NULL,
    from_item_id BIGINT NOT NULL,
    to_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    actual_yield INT,
    status VARCHAR(20) NOT NULL,
    start_time DATETIME,
    finish_time DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_recycle_planning_detail FOREIGN KEY (planning_detail_id) REFERENCES planning_detail(id),
    CONSTRAINT fk_recycle_from_item FOREIGN KEY (from_item_id) REFERENCES item(id),
    CONSTRAINT fk_recycle_to_item FOREIGN KEY (to_item_id) REFERENCES item(id)
);

CREATE TABLE assembly_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assembly_no VARCHAR(50) NOT NULL UNIQUE,
    planning_detail_id BIGINT NOT NULL,
    set_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_assembly_planning_detail FOREIGN KEY (planning_detail_id) REFERENCES planning_detail(id),
    CONSTRAINT fk_assembly_set_item FOREIGN KEY (set_item_id) REFERENCES item(id)
);

CREATE TABLE transfer_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transfer_no VARCHAR(50) NOT NULL UNIQUE,
    from_warehouse_id BIGINT NOT NULL,
    to_warehouse_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_transfer_from_wh FOREIGN KEY (from_warehouse_id) REFERENCES warehouse(id),
    CONSTRAINT fk_transfer_to_wh FOREIGN KEY (to_warehouse_id) REFERENCES warehouse(id),
    CONSTRAINT fk_transfer_item FOREIGN KEY (item_id) REFERENCES item(id)
);

CREATE INDEX idx_purchase_req_no ON purchase_request(request_no);
CREATE INDEX idx_recycle_order_no ON recycle_order(order_no);
CREATE INDEX idx_assembly_no ON assembly_order(assembly_no);
CREATE INDEX idx_transfer_no ON transfer_order(transfer_no);
