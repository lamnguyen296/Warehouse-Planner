CREATE TABLE warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

CREATE TABLE location (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    zone VARCHAR(50),
    rack VARCHAR(50),
    capacity INT,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_location_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse(id),
    CONSTRAINT uq_location_code_warehouse UNIQUE (warehouse_id, code)
);

CREATE TABLE item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    item_type VARCHAR(30) NOT NULL,
    unit VARCHAR(20),
    description TEXT,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

CREATE TABLE bom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_item_id BIGINT NOT NULL,
    child_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    priority INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_bom_parent FOREIGN KEY (parent_item_id) REFERENCES item(id),
    CONSTRAINT fk_bom_child FOREIGN KEY (child_item_id) REFERENCES item(id),
    CONSTRAINT uq_bom_parent_child UNIQUE (parent_item_id, child_item_id)
);

CREATE INDEX idx_warehouse_type ON warehouse(type);
CREATE INDEX idx_item_type ON item(item_type);
CREATE INDEX idx_bom_parent ON bom(parent_item_id);
CREATE INDEX idx_bom_child ON bom(child_item_id);
CREATE INDEX idx_location_warehouse ON location(warehouse_id);
