CREATE TABLE workshop_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_no VARCHAR(50) NOT NULL UNIQUE,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_date DATETIME,
    expected_date DATETIME,
    created_by VARCHAR(50),
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

CREATE TABLE workshop_request_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workshop_request_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_wrd_request FOREIGN KEY (workshop_request_id) REFERENCES workshop_request(id),
    CONSTRAINT fk_wrd_item FOREIGN KEY (item_id) REFERENCES item(id)
);

CREATE TABLE planning (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    planning_no VARCHAR(50) NOT NULL UNIQUE,
    workshop_request_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_planning_request FOREIGN KEY (workshop_request_id) REFERENCES workshop_request(id)
);

CREATE TABLE planning_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    planning_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    required_quantity INT NOT NULL DEFAULT 0,
    available_quantity INT NOT NULL DEFAULT 0,
    recycle_quantity INT NOT NULL DEFAULT 0,
    purchase_quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    action VARCHAR(50),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_pd_planning FOREIGN KEY (planning_id) REFERENCES planning(id),
    CONSTRAINT fk_pd_item FOREIGN KEY (item_id) REFERENCES item(id)
);

-- Note: We add a foreign key from inventory_reservation to planning_detail.
-- Since inventory_reservation was created in V3, we add the constraint here.
ALTER TABLE inventory_reservation
ADD CONSTRAINT fk_inv_res_planning_detail FOREIGN KEY (planning_detail_id) REFERENCES planning_detail(id);

CREATE INDEX idx_wr_status ON workshop_request(status);
CREATE INDEX idx_planning_status ON planning(status);
