ALTER TABLE recycle_order
    ADD COLUMN expected_yield INT NULL AFTER quantity,
    ADD CONSTRAINT chk_recycle_expected_yield
        CHECK (expected_yield IS NULL OR expected_yield > 0);

ALTER TABLE transfer_order
    ADD COLUMN planning_id BIGINT NULL AFTER transfer_no,
    ADD CONSTRAINT fk_transfer_planning
        FOREIGN KEY (planning_id) REFERENCES planning(id);

CREATE INDEX idx_transfer_planning_status
    ON transfer_order(planning_id, status);
