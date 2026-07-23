package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.request.PlanningRequest;
import com.restapi.wmsservice.dto.response.PlanningResponse;
import com.restapi.wmsservice.enums.PlanningStatus;

import java.util.List;

public interface PlanningService {

    // ── CRUD ──────────────────────────────────────────────────────────────
    PlanningResponse createPlanning(PlanningRequest request);
    PlanningResponse getPlanning(Long id);
    List<PlanningResponse> getAllPlannings();
    PlanningResponse updatePlanning(Long id, PlanningRequest request);
    void deletePlanning(Long id);

    // ── Business Flow (Phase 6 – Planning Engine) ─────────────────────────
    /**
     * Core MRP Engine: nhận WorkshopRequest đã APPROVED → BOM Explosion →
     * kiểm tra Inventory → xác định PlanningAction → sinh Planning + PlanningDetails.
     *
     * Flow:
     *  1. Validate WorkshopRequest status = APPROVED
     *  2. BOM Explosion (WITH RECURSIVE CTE) cho từng SET item trong request
     *  3. Aggregate required quantities (gộp các item trùng từ nhiều SET)
     *  4. Check available inventory trên COMPONENT_WAREHOUSE
     *  5. Xác định action cho từng component:
     *     - available >= required → USE_AVAILABLE
     *     - raw component tồn tại và đủ → RECYCLE
     *     - không đủ raw → PURCHASE
     *     - đây là SET → ASSEMBLE
     *  6. Tạo Planning entity + PlanningDetail list
     *  7. Trả về PlanningResponse
     *
     * @param workshopRequestId ID của WorkshopRequest đã APPROVED
     * @return PlanningResponse vừa được tạo
     */
    PlanningResponse runPlanningEngine(Long workshopRequestId);

    /**
     * Duyệt Planning: PLANNING → APPROVED.
     * Chỉ thực hiện khi Planning status = PLANNING.
     */
    PlanningResponse approvePlanning(Long id);

    PlanningResponse startExecution(Long id);

    PlanningResponse failPlanning(Long id);

    /**
     * Lấy tất cả Planning của một WorkshopRequest.
     */
    List<PlanningResponse> getByWorkshopRequest(Long workshopRequestId);

    /**
     * Lấy Planning theo status.
     */
    List<PlanningResponse> getByStatus(PlanningStatus status);
}
