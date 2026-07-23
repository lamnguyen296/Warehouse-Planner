package com.restapi.wmsservice.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    WAREHOUSE_NOT_FOUND(2001, "Warehouse not found", HttpStatus.NOT_FOUND),
    WAREHOUSE_CODE_EXISTED(2002, "Warehouse code existed", HttpStatus.BAD_REQUEST),
    ACTIVE_WAREHOUSE_TYPE_EXISTED(2011, "Only one active warehouse is allowed for each warehouse type", HttpStatus.CONFLICT),
    LOCATION_NOT_FOUND(2003, "Location not found", HttpStatus.NOT_FOUND),
    LOCATION_CODE_EXISTED(2004, "Location code existed", HttpStatus.BAD_REQUEST),
    ITEM_NOT_FOUND(2005, "Item not found", HttpStatus.NOT_FOUND),
    ITEM_CODE_EXISTED(2006, "Item code existed", HttpStatus.BAD_REQUEST),
    BOM_NOT_FOUND(2007, "BOM not found", HttpStatus.NOT_FOUND),
    BOM_RELATION_EXISTED(2008, "BOM relationship already existed", HttpStatus.BAD_REQUEST),
    BOM_PARENT_CHILD_SAME(2009, "Parent and child item cannot be the same", HttpStatus.BAD_REQUEST),
    BOM_CYCLE_DETECTED(2010, "BOM cycle detected", HttpStatus.BAD_REQUEST),
    INVENTORY_NOT_FOUND(3001, "Inventory not found", HttpStatus.NOT_FOUND),
    BATCH_NOT_FOUND(3002, "Batch not found", HttpStatus.NOT_FOUND),
    TRANSACTION_NOT_FOUND(3003, "Transaction not found", HttpStatus.NOT_FOUND),
    RESERVATION_NOT_FOUND(3004, "Reservation not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK(3005, "Insufficient stock", HttpStatus.BAD_REQUEST),
    INVENTORY_BATCH_STOCK_NOT_FOUND(3006, "Inventory batch stock not found", HttpStatus.NOT_FOUND),
    WORKSHOP_REQUEST_NOT_FOUND(4001, "Workshop request not found", HttpStatus.NOT_FOUND),
    WORKSHOP_REQUEST_DETAIL_NOT_FOUND(4002, "Workshop request detail not found", HttpStatus.NOT_FOUND),
    PLANNING_NOT_FOUND(4003, "Planning not found", HttpStatus.NOT_FOUND),
    PLANNING_DETAIL_NOT_FOUND(4004, "Planning detail not found", HttpStatus.NOT_FOUND),
    INVALID_ITEM_TYPE(4005, "Item type must be SET for Workshop Request", HttpStatus.BAD_REQUEST),
    OPTIMISTIC_LOCKING_ERROR(3007, "Data has been updated by another transaction, please retry", HttpStatus.CONFLICT),
    DATA_INTEGRITY_VIOLATION(1009, "Data integrity violation", HttpStatus.CONFLICT),
    VALIDATION_ERROR(1010, "Validation failed", HttpStatus.BAD_REQUEST),
    ACCOUNT_INACTIVE(1011, "Account is inactive", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(1012, "Account is locked", HttpStatus.UNAUTHORIZED),
    ROLE_NOT_FOUND(1013, "One or more roles do not exist", HttpStatus.BAD_REQUEST),
    ROLES_REQUIRED(1014, "At least one role is required", HttpStatus.BAD_REQUEST),
    FULLNAME_REQUIRED(1015, "Full name is required", HttpStatus.BAD_REQUEST),
    USERNAME_REQUIRED(1016, "Username is required", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1017, "Password is required", HttpStatus.BAD_REQUEST),
    USER_STATUS_REQUIRED(1018, "User status is required", HttpStatus.BAD_REQUEST),
    SELF_SECURITY_CHANGE_NOT_ALLOWED(1019, "You cannot change your own status or roles", HttpStatus.CONFLICT),
    SELF_DELETE_NOT_ALLOWED(1020, "You cannot delete your own account", HttpStatus.CONFLICT),
    NOTIFICATION_NOT_FOUND(1021, "Notification not found", HttpStatus.NOT_FOUND),
    PURCHASE_REQUEST_NOT_FOUND(5001, "Purchase request not found", HttpStatus.NOT_FOUND),
    RECYCLE_ORDER_NOT_FOUND(5002, "Recycle order not found", HttpStatus.NOT_FOUND),
    ASSEMBLY_ORDER_NOT_FOUND(5003, "Assembly order not found", HttpStatus.NOT_FOUND),
    TRANSFER_ORDER_NOT_FOUND(5004, "Transfer order not found", HttpStatus.NOT_FOUND),

    // Phase 6 – Workshop Request Flow (6001–6099)
    INVALID_REQUEST_STATUS_TRANSITION(6001, "Invalid status transition for workshop request", HttpStatus.BAD_REQUEST),
    WORKSHOP_REQUEST_NOT_EDITABLE(6002, "Workshop request cannot be modified in current status", HttpStatus.BAD_REQUEST),
    WORKSHOP_REQUEST_NOT_CANCELLABLE(6003, "Workshop request cannot be cancelled in current status", HttpStatus.BAD_REQUEST),
    WORKSHOP_REQUEST_ALREADY_SUBMITTED(6004, "Workshop request has already been submitted", HttpStatus.BAD_REQUEST),
    WORKSHOP_REQUEST_NOT_SUBMITTED(6005, "Workshop request must be in SUBMITTED status to approve", HttpStatus.BAD_REQUEST),
    INVALID_EXPECTED_DATE(6006, "Expected date must be after requested date and in the future", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY(6007, "Quantity must be greater than 0", HttpStatus.BAD_REQUEST),
    DUPLICATE_ITEM_IN_REQUEST(6008, "Duplicate item found in workshop request details", HttpStatus.BAD_REQUEST),
    DETAILS_REQUIRED_FOR_SUBMIT(6009, "Workshop request must have at least one detail to submit", HttpStatus.BAD_REQUEST),

    // Phase 6 – Planning Engine Flow (6100–6199)
    WORKSHOP_REQUEST_NOT_APPROVED(6101, "Workshop request must be in APPROVED status to run planning", HttpStatus.BAD_REQUEST),
    PLANNING_ENGINE_NO_BOM(6102, "No BOM definition found for SET item, cannot run planning", HttpStatus.BAD_REQUEST),
    PLANNING_ALREADY_RUNNING(6103, "A planning is already in PLANNING status for this request", HttpStatus.BAD_REQUEST),
    PLANNING_NOT_EDITABLE(6104, "Planning cannot be modified in current status", HttpStatus.BAD_REQUEST),
    PLANNING_QUANTITY_OVERFLOW(6105, "Calculated planning quantity exceeds supported range", HttpStatus.BAD_REQUEST),

    // Phase 6 – Inventory Operations Flow (6200–6299)
    INSUFFICIENT_AVAILABLE_STOCK(6201, "Insufficient available stock to reserve", HttpStatus.BAD_REQUEST),
    INVENTORY_NOT_FOUND_FOR_ITEM(6202, "No inventory record found for item in target warehouse", HttpStatus.NOT_FOUND),
    RESERVATION_ALREADY_RELEASED(6203, "Reservation has already been released or expired", HttpStatus.BAD_REQUEST),
    RECYCLE_ORDER_INVALID_STATUS(6204, "Recycle order status does not allow this operation", HttpStatus.BAD_REQUEST),
    ASSEMBLY_ORDER_INVALID_STATUS(6205, "Assembly order status does not allow this operation", HttpStatus.BAD_REQUEST),
    PURCHASE_REQUEST_INVALID_STATUS(6206, "Purchase request status does not allow this operation", HttpStatus.BAD_REQUEST),
    OPTIMISTIC_LOCK_RETRY_EXHAUSTED(6207, "Inventory update failed after retries due to concurrent modification", HttpStatus.CONFLICT),
    RECEIVE_QUANTITY_EXCEEDS_ORDERED(6208, "Received quantity exceeds ordered quantity", HttpStatus.BAD_REQUEST),
    ASSEMBLY_INSUFFICIENT_COMPONENTS(6209, "Insufficient component stock to complete assembly", HttpStatus.BAD_REQUEST),
    RESERVATION_ITEM_MISMATCH(6210, "Reservation item must match planning detail item", HttpStatus.BAD_REQUEST),
    RECYCLE_YIELD_EXCEEDS_EXPECTED(6211, "Recycle yield exceeds BOM conversion output", HttpStatus.BAD_REQUEST),
    INVALID_WAREHOUSE_TYPE(6212, "Warehouse type is not valid for this inventory operation", HttpStatus.BAD_REQUEST),
    ORDER_PLANNING_ITEM_MISMATCH(6213, "Order item must match planning detail item", HttpStatus.BAD_REQUEST),
    INVALID_BOM_ITEM_RELATION(6214, "BOM item types do not form a supported assembly or recycle relation", HttpStatus.BAD_REQUEST),
    PLANNING_INVALID_STATUS(6215, "Planning status does not allow this operation", HttpStatus.BAD_REQUEST),
    PLANNING_ORDER_QUANTITY_EXCEEDED(6216, "Order quantity exceeds the remaining planned quantity", HttpStatus.BAD_REQUEST),
    ACTIVE_WAREHOUSE_NOT_CONFIGURED(6217, "Exactly one active warehouse must be configured for this warehouse type", HttpStatus.CONFLICT),
    INVENTORY_IDENTITY_IMMUTABLE(6218, "Inventory warehouse, location and item cannot be changed", HttpStatus.BAD_REQUEST),
    RESERVED_QUANTITY_SYSTEM_MANAGED(6219, "Reserved quantity can only be changed by reservation operations", HttpStatus.BAD_REQUEST),
    INVENTORY_NOT_EMPTY(6220, "Inventory with stock or reservations cannot be deleted", HttpStatus.CONFLICT),
    PLANNING_HAS_NON_CANCELLABLE_OPERATIONS(6221, "Planning has operations that can no longer be cancelled", HttpStatus.CONFLICT),

    // Phase 6 – Transfer Flow (6300–6399)
    TRANSFER_ORDER_INVALID_STATUS(6301, "Transfer order status does not allow this operation", HttpStatus.BAD_REQUEST),
    TRANSFER_SAME_WAREHOUSE(6302, "Source and destination warehouse cannot be the same", HttpStatus.BAD_REQUEST),
    TRANSFER_INSUFFICIENT_STOCK(6303, "Insufficient stock in source warehouse for transfer", HttpStatus.BAD_REQUEST),

    // Media storage (7000-7099)
    MEDIA_STORAGE_NOT_CONFIGURED(7001, "Media storage is not configured", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_MEDIA_FILE(7002, "Only JPEG, PNG and WebP images are supported", HttpStatus.BAD_REQUEST),
    MEDIA_FILE_TOO_LARGE(7003, "Image must not exceed 5 MB", HttpStatus.PAYLOAD_TOO_LARGE),
    MEDIA_UPLOAD_FAILED(7004, "Unable to upload image", HttpStatus.BAD_GATEWAY),
    ATTACHMENT_NOT_FOUND(7005, "Attachment not found", HttpStatus.NOT_FOUND),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;
}
