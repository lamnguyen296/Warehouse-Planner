package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.request.RecycleOrderRequest;
import com.restapi.wmsservice.dto.response.RecycleOrderResponse;

import java.util.List;

public interface RecycleOrderService {
    RecycleOrderResponse create(RecycleOrderRequest request);
    RecycleOrderResponse get(Long id);
    List<RecycleOrderResponse> getAll();
    RecycleOrderResponse update(Long id, RecycleOrderRequest request);
    void delete(Long id);

    // ── Business Flow (Phase 6 – Flow 3) ──────────────────────────────────
    /** Chuyển RecycleOrder PENDING → IN_PROGRESS. */
    RecycleOrderResponse startRecycle(Long id);

    /** Chuyển RecycleOrder từ PENDING/IN_PROGRESS → FAILED (cancel). */
    RecycleOrderResponse cancelRecycle(Long id);
}
