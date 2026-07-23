package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.request.PurchaseRequestRequest;
import com.restapi.wmsservice.dto.response.PurchaseRequestResponse;
import com.restapi.wmsservice.enums.PurchaseStatus;

import java.util.List;

public interface PurchaseRequestService {
    PurchaseRequestResponse create(PurchaseRequestRequest request);
    PurchaseRequestResponse get(Long id);
    List<PurchaseRequestResponse> getAll();
    PurchaseRequestResponse update(Long id, PurchaseRequestRequest request);
    void delete(Long id);

    // ── Business Flow ─────────────────────────────────────────────────────

    /** Duyệt PurchaseRequest: PENDING → APPROVED. */
    PurchaseRequestResponse approvePurchase(Long id);

    /** Chuyển trạng thái sang đã đặt hàng: APPROVED → ORDERED. */
    PurchaseRequestResponse orderPurchase(Long id);

    /** Hủy PurchaseRequest: PENDING/APPROVED → CANCELLED. */
    PurchaseRequestResponse cancelPurchase(Long id);

    /** Lấy PurchaseRequest theo trạng thái. */
    List<PurchaseRequestResponse> getByStatus(PurchaseStatus status);
}
