package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.request.InventoryReservationRequest;
import com.restapi.wmsservice.dto.response.InventoryReservationResponse;

import java.util.List;

public interface InventoryReservationService {
    InventoryReservationResponse createReservation(InventoryReservationRequest request);
    InventoryReservationResponse getReservation(Long id);
    List<InventoryReservationResponse> getAllReservations();
    InventoryReservationResponse updateReservation(Long id, InventoryReservationRequest request);
    void deleteReservation(Long id);
}
