package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.request.PlanningRequest;
import com.restapi.wmsservice.dto.response.PlanningResponse;
import com.restapi.wmsservice.enums.PlanningStatus;
import com.restapi.wmsservice.service.PlanningService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plannings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlanningController {

    PlanningService planningService;

    // ── CRUD ──────────────────────────────────────────────────────────────

    @PostMapping
    public ApiResponse<PlanningResponse> createPlanning(@RequestBody @Valid PlanningRequest request) {
        return ApiResponse.<PlanningResponse>builder()
                .result(planningService.createPlanning(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PlanningResponse> getPlanning(@PathVariable Long id) {
        return ApiResponse.<PlanningResponse>builder()
                .result(planningService.getPlanning(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<PlanningResponse>> getAllPlannings() {
        return ApiResponse.<List<PlanningResponse>>builder()
                .result(planningService.getAllPlannings())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<PlanningResponse> updatePlanning(
            @PathVariable Long id,
            @RequestBody @Valid PlanningRequest request) {
        return ApiResponse.<PlanningResponse>builder()
                .result(planningService.updatePlanning(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deletePlanning(@PathVariable Long id) {
        planningService.deletePlanning(id);
        return ApiResponse.<String>builder()
                .result("Planning deleted successfully")
                .build();
    }

    // ── Business Flow (Phase 6 – Planning Engine) ─────────────────────────

    /**
     * Chạy MRP Planning Engine: APPROVED WorkshopRequest → Planning + PlanningDetails
     * POST /plannings/run-engine/{workshopRequestId}
     */
    @PostMapping("/run-engine/{workshopRequestId}")
    public ApiResponse<PlanningResponse> runPlanningEngine(@PathVariable Long workshopRequestId) {
        return ApiResponse.<PlanningResponse>builder()
                .result(planningService.runPlanningEngine(workshopRequestId))
                .build();
    }

    /**
     * Duyệt Planning: PLANNING → APPROVED
     * POST /plannings/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ApiResponse<PlanningResponse> approvePlanning(@PathVariable Long id) {
        return ApiResponse.<PlanningResponse>builder()
                .result(planningService.approvePlanning(id))
                .build();
    }

    @PostMapping("/{id}/start-execution")
    public ApiResponse<PlanningResponse> startExecution(@PathVariable Long id) {
        return ApiResponse.<PlanningResponse>builder()
                .result(planningService.startExecution(id))
                .build();
    }

    @PostMapping("/{id}/fail")
    public ApiResponse<PlanningResponse> failPlanning(@PathVariable Long id) {
        return ApiResponse.<PlanningResponse>builder()
                .result(planningService.failPlanning(id))
                .build();
    }

    /**
     * Lấy tất cả Planning của một WorkshopRequest
     * GET /plannings/by-workshop-request/{workshopRequestId}
     */
    @GetMapping("/by-workshop-request/{workshopRequestId}")
    public ApiResponse<List<PlanningResponse>> getByWorkshopRequest(@PathVariable Long workshopRequestId) {
        return ApiResponse.<List<PlanningResponse>>builder()
                .result(planningService.getByWorkshopRequest(workshopRequestId))
                .build();
    }

    /**
     * Lấy Planning theo status
     * GET /plannings/by-status?status=PLANNING
     */
    @GetMapping("/by-status")
    public ApiResponse<List<PlanningResponse>> getByStatus(@RequestParam PlanningStatus status) {
        return ApiResponse.<List<PlanningResponse>>builder()
                .result(planningService.getByStatus(status))
                .build();
    }
}
