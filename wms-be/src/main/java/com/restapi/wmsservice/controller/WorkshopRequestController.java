package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.request.WorkshopRequestRequest;
import com.restapi.wmsservice.dto.response.WorkshopRequestResponse;
import com.restapi.wmsservice.enums.RequestStatus;
import com.restapi.wmsservice.service.WorkshopRequestService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workshop-requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkshopRequestController {

    WorkshopRequestService workshopRequestService;

    // ── CRUD ──────────────────────────────────────────────────────────────

    @PostMapping
    public ApiResponse<WorkshopRequestResponse> createRequest(@RequestBody @Valid WorkshopRequestRequest request) {
        return ApiResponse.<WorkshopRequestResponse>builder()
                .result(workshopRequestService.createRequest(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkshopRequestResponse> getRequest(@PathVariable Long id) {
        return ApiResponse.<WorkshopRequestResponse>builder()
                .result(workshopRequestService.getRequest(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<WorkshopRequestResponse>> getAllRequests() {
        return ApiResponse.<List<WorkshopRequestResponse>>builder()
                .result(workshopRequestService.getAllRequests())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkshopRequestResponse> updateRequest(
            @PathVariable Long id,
            @RequestBody @Valid WorkshopRequestRequest request) {
        return ApiResponse.<WorkshopRequestResponse>builder()
                .result(workshopRequestService.updateRequest(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteRequest(@PathVariable Long id) {
        workshopRequestService.deleteRequest(id);
        return ApiResponse.<String>builder()
                .result("Workshop request deleted successfully")
                .build();
    }

    // ── Business Flow (Phase 6) ───────────────────────────────────────────

    /**
     * Gửi yêu cầu: DRAFT → SUBMITTED
     */
    @PostMapping("/{id}/submit")
    public ApiResponse<WorkshopRequestResponse> submitRequest(@PathVariable Long id) {
        return ApiResponse.<WorkshopRequestResponse>builder()
                .result(workshopRequestService.submitRequest(id))
                .build();
    }

    /**
     * Duyệt yêu cầu: SUBMITTED → APPROVED
     */
    @PostMapping("/{id}/approve")
    public ApiResponse<WorkshopRequestResponse> approveRequest(@PathVariable Long id) {
        return ApiResponse.<WorkshopRequestResponse>builder()
                .result(workshopRequestService.approveRequest(id))
                .build();
    }

    /**
     * Huỷ yêu cầu: DRAFT | SUBMITTED → CANCELLED
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<WorkshopRequestResponse> cancelRequest(@PathVariable Long id) {
        return ApiResponse.<WorkshopRequestResponse>builder()
                .result(workshopRequestService.cancelRequest(id))
                .build();
    }

    /**
     * Lấy danh sách theo status
     */
    @GetMapping("/by-status")
    public ApiResponse<List<WorkshopRequestResponse>> getByStatus(
            @RequestParam RequestStatus status) {
        return ApiResponse.<List<WorkshopRequestResponse>>builder()
                .result(workshopRequestService.getByStatus(status))
                .build();
    }
}

