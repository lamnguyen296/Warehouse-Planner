package com.restapi.wmsservice.security;

public final class PermissionCode {

    public static final String SECURITY_MANAGE = "SECURITY_MANAGE";
    public static final String MASTER_DATA_READ = "MASTER_DATA_READ";
    public static final String MASTER_DATA_MANAGE = "MASTER_DATA_MANAGE";

    public static final String WORKSHOP_REQUEST_CREATE = "WORKSHOP_REQUEST_CREATE";
    public static final String WORKSHOP_REQUEST_READ_OWN = "WORKSHOP_REQUEST_READ_OWN";
    public static final String WORKSHOP_REQUEST_READ_ALL = "WORKSHOP_REQUEST_READ_ALL";
    public static final String WORKSHOP_REQUEST_UPDATE_OWN = "WORKSHOP_REQUEST_UPDATE_OWN";
    public static final String WORKSHOP_REQUEST_SUBMIT_OWN = "WORKSHOP_REQUEST_SUBMIT_OWN";
    public static final String WORKSHOP_REQUEST_CANCEL_OWN = "WORKSHOP_REQUEST_CANCEL_OWN";
    public static final String WORKSHOP_REQUEST_APPROVE = "WORKSHOP_REQUEST_APPROVE";

    public static final String PLANNING_READ = "PLANNING_READ";
    public static final String PLANNING_RUN = "PLANNING_RUN";
    public static final String PLANNING_APPROVE = "PLANNING_APPROVE";
    public static final String PLANNING_EXECUTE = "PLANNING_EXECUTE";
    public static final String PLANNING_FAIL = "PLANNING_FAIL";

    public static final String INVENTORY_READ = "INVENTORY_READ";
    public static final String INVENTORY_ADJUST = "INVENTORY_ADJUST";
    public static final String INVENTORY_RESERVATION_READ = "INVENTORY_RESERVATION_READ";
    public static final String INVENTORY_MOVEMENT_READ = "INVENTORY_MOVEMENT_READ";

    public static final String PURCHASE_READ = "PURCHASE_READ";
    public static final String PURCHASE_EXECUTE = "PURCHASE_EXECUTE";
    public static final String RECYCLE_READ = "RECYCLE_READ";
    public static final String RECYCLE_EXECUTE = "RECYCLE_EXECUTE";
    public static final String ASSEMBLY_READ = "ASSEMBLY_READ";
    public static final String ASSEMBLY_EXECUTE = "ASSEMBLY_EXECUTE";
    public static final String TRANSFER_READ = "TRANSFER_READ";
    public static final String TRANSFER_EXECUTE = "TRANSFER_EXECUTE";

    private PermissionCode() {
    }
}
