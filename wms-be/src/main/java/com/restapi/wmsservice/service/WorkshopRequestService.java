package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.request.WorkshopRequestRequest;
import com.restapi.wmsservice.dto.response.WorkshopRequestResponse;
import com.restapi.wmsservice.enums.RequestStatus;

import java.util.List;

public interface WorkshopRequestService {

    // ── CRUD ──────────────────────────────────────────────────────────────
    WorkshopRequestResponse createRequest(WorkshopRequestRequest request);
    WorkshopRequestResponse getRequest(Long id);
    List<WorkshopRequestResponse> getAllRequests();
    WorkshopRequestResponse updateRequest(Long id, WorkshopRequestRequest request);
    void deleteRequest(Long id);

    // ── Business Flow (Phase 6) ───────────────────────────────────────────
    /**
     * DRAFT → SUBMITTED: Xác nhận gửi yêu cầu lên Kho Sẵn.
     * Validate: status phải là DRAFT, phải có ít nhất 1 detail.
     */
    WorkshopRequestResponse submitRequest(Long id);

    /**
     * SUBMITTED → APPROVED: Kho Sẵn duyệt yêu cầu.
     * Validate: status phải là SUBMITTED.
     */
    WorkshopRequestResponse approveRequest(Long id);

    /**
     * ANY cancellable status → CANCELLED.
     * Cancellable: DRAFT, SUBMITTED.
     * Không cancel được khi đang APPROVED / IN_PROGRESS / COMPLETED.
     * Validate: không có Planning đang active.
     */
    WorkshopRequestResponse cancelRequest(Long id);

    /**
     * Lấy danh sách theo status.
     */
    List<WorkshopRequestResponse> getByStatus(RequestStatus status);
}

