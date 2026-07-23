package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.request.AssemblyOrderRequest;
import com.restapi.wmsservice.dto.response.AssemblyOrderResponse;

import java.util.List;

public interface AssemblyOrderService {
    AssemblyOrderResponse create(AssemblyOrderRequest request);
    AssemblyOrderResponse get(Long id);
    List<AssemblyOrderResponse> getAll();
    AssemblyOrderResponse update(Long id, AssemblyOrderRequest request);
    void delete(Long id);

    // ── Business Flow (Phase 6 – Flow 3) ──────────────────────────────────
    /** Chuyển AssemblyOrder PENDING → IN_PROGRESS. */
    AssemblyOrderResponse startAssembly(Long id);

    /** Hủy AssemblyOrder (PENDING/IN_PROGRESS → FAILED). */
    AssemblyOrderResponse cancelAssembly(Long id);
}
