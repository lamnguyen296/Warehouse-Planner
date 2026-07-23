package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.response.InventoryReservationResponse;
import com.restapi.wmsservice.service.InventoryReservationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory-reservations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryReservationController {

    InventoryReservationService reservationService;

    @GetMapping("/{id}")
    public ApiResponse<InventoryReservationResponse> getReservation(@PathVariable Long id) {
        return ApiResponse.<InventoryReservationResponse>builder()
                .result(reservationService.getReservation(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<InventoryReservationResponse>> getAllReservations() {
        return ApiResponse.<List<InventoryReservationResponse>>builder()
                .result(reservationService.getAllReservations())
                .build();
    }

}
