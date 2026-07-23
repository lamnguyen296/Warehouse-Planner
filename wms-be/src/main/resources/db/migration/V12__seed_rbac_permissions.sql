INSERT IGNORE INTO role (name, description) VALUES
    ('ADMIN', 'System administrator'),
    ('OPERATOR', 'WMS operations user'),
    ('WORKSHOP', 'Workshop requester'),
    ('MANAGER', 'Approval and monitoring user');

INSERT INTO permission (code, name, module, description) VALUES
    ('SECURITY_MANAGE', 'Manage security', 'SECURITY', 'Manage users, roles and permissions'),
    ('MASTER_DATA_READ', 'Read master data', 'MASTER_DATA', 'Read warehouses, locations, items, BOM and batches'),
    ('MASTER_DATA_MANAGE', 'Manage master data', 'MASTER_DATA', 'Create, update and delete master data'),
    ('WORKSHOP_REQUEST_CREATE', 'Create workshop request', 'WORKSHOP', 'Create workshop request drafts'),
    ('WORKSHOP_REQUEST_READ_OWN', 'Read own workshop requests', 'WORKSHOP', 'Read workshop requests created by the current user'),
    ('WORKSHOP_REQUEST_READ_ALL', 'Read all workshop requests', 'WORKSHOP', 'Read workshop requests created by any user'),
    ('WORKSHOP_REQUEST_UPDATE_OWN', 'Update own workshop requests', 'WORKSHOP', 'Update or delete own workshop request drafts'),
    ('WORKSHOP_REQUEST_SUBMIT_OWN', 'Submit own workshop requests', 'WORKSHOP', 'Submit own workshop request drafts'),
    ('WORKSHOP_REQUEST_CANCEL_OWN', 'Cancel own workshop requests', 'WORKSHOP', 'Cancel own eligible workshop requests'),
    ('WORKSHOP_REQUEST_APPROVE', 'Approve workshop requests', 'WORKSHOP', 'Approve submitted workshop requests'),
    ('PLANNING_READ', 'Read planning', 'PLANNING', 'Read planning and MRP results'),
    ('PLANNING_RUN', 'Run MRP planning', 'PLANNING', 'Generate planning from an approved workshop request'),
    ('PLANNING_APPROVE', 'Approve planning', 'PLANNING', 'Approve an MRP planning result'),
    ('PLANNING_EXECUTE', 'Execute planning', 'PLANNING', 'Start planning execution and manage planning drafts'),
    ('PLANNING_FAIL', 'Fail planning', 'PLANNING', 'Fail an executing planning and release eligible resources'),
    ('INVENTORY_READ', 'Read inventory', 'INVENTORY', 'Read physical, reserved and available stock'),
    ('INVENTORY_ADJUST', 'Adjust inventory', 'INVENTORY', 'Create and adjust inventory records and reservations'),
    ('INVENTORY_RESERVATION_READ', 'Read reservations', 'INVENTORY', 'Read inventory reservation history'),
    ('INVENTORY_MOVEMENT_READ', 'Read movements', 'INVENTORY', 'Read inventory transaction history'),
    ('PURCHASE_READ', 'Read purchase requests', 'PURCHASE', 'Read manual and planned purchase requests'),
    ('PURCHASE_EXECUTE', 'Execute purchase requests', 'PURCHASE', 'Create, approve, order, receive and cancel purchases'),
    ('RECYCLE_READ', 'Read recycle orders', 'RECYCLE', 'Read manual and planned recycle orders'),
    ('RECYCLE_EXECUTE', 'Execute recycle orders', 'RECYCLE', 'Create, start, complete and cancel recycle orders'),
    ('ASSEMBLY_READ', 'Read assembly orders', 'ASSEMBLY', 'Read manual and planned assembly orders'),
    ('ASSEMBLY_EXECUTE', 'Execute assembly orders', 'ASSEMBLY', 'Create, start, complete and cancel assembly orders'),
    ('TRANSFER_READ', 'Read transfer orders', 'TRANSFER', 'Read manual and planned transfer or issue orders'),
    ('TRANSFER_EXECUTE', 'Execute transfer orders', 'TRANSFER', 'Create, execute, complete and cancel transfers or issues')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    module = VALUES(module),
    description = VALUES(description);

INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT role.id, permission.id
FROM role
CROSS JOIN permission
WHERE role.name = 'ADMIN';

INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT role.id, permission.id
FROM role
JOIN permission ON permission.code IN (
    'MASTER_DATA_READ',
    'WORKSHOP_REQUEST_READ_ALL',
    'PLANNING_READ', 'PLANNING_RUN', 'PLANNING_EXECUTE',
    'INVENTORY_READ', 'INVENTORY_ADJUST', 'INVENTORY_RESERVATION_READ', 'INVENTORY_MOVEMENT_READ',
    'PURCHASE_READ', 'PURCHASE_EXECUTE',
    'RECYCLE_READ', 'RECYCLE_EXECUTE',
    'ASSEMBLY_READ', 'ASSEMBLY_EXECUTE',
    'TRANSFER_READ', 'TRANSFER_EXECUTE'
)
WHERE role.name = 'OPERATOR';

INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT role.id, permission.id
FROM role
JOIN permission ON permission.code IN (
    'MASTER_DATA_READ',
    'WORKSHOP_REQUEST_READ_ALL', 'WORKSHOP_REQUEST_APPROVE',
    'PLANNING_READ', 'PLANNING_APPROVE', 'PLANNING_FAIL',
    'INVENTORY_READ', 'INVENTORY_RESERVATION_READ', 'INVENTORY_MOVEMENT_READ',
    'PURCHASE_READ', 'RECYCLE_READ', 'ASSEMBLY_READ', 'TRANSFER_READ'
)
WHERE role.name = 'MANAGER';

INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT role.id, permission.id
FROM role
JOIN permission ON permission.code IN (
    'MASTER_DATA_READ',
    'WORKSHOP_REQUEST_CREATE', 'WORKSHOP_REQUEST_READ_OWN', 'WORKSHOP_REQUEST_UPDATE_OWN',
    'WORKSHOP_REQUEST_SUBMIT_OWN', 'WORKSHOP_REQUEST_CANCEL_OWN'
)
WHERE role.name = 'WORKSHOP';
