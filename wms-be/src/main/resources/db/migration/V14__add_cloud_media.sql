ALTER TABLE item
    ADD COLUMN image_public_id VARCHAR(255) NULL,
    ADD COLUMN image_url VARCHAR(1000) NULL;

CREATE TABLE inventory_transaction_attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    public_id VARCHAR(255) NOT NULL,
    secure_url VARCHAR(1000) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_by VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT uq_inv_txn_attachment_public_id UNIQUE (public_id),
    CONSTRAINT fk_inv_txn_attachment_transaction
        FOREIGN KEY (transaction_id) REFERENCES inventory_transaction(id)
);

CREATE INDEX idx_inv_txn_attachment_transaction
    ON inventory_transaction_attachment(transaction_id, created_at);
