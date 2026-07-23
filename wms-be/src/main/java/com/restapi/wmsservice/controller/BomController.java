package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.request.BomRequest;
import com.restapi.wmsservice.dto.response.BomResponse;
import com.restapi.wmsservice.service.BomService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomController {

    BomService bomService;

    @PostMapping
    public ApiResponse<BomResponse> create(@RequestBody @Valid BomRequest request) {
        return ApiResponse.<BomResponse>builder()
                .result(bomService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<BomResponse>> getAll(@RequestParam(required = false) Long parentItemId) {
        List<BomResponse> result = (parentItemId != null) 
            ? bomService.getByParentItemId(parentItemId) 
            : bomService.getAll();

        return ApiResponse.<List<BomResponse>>builder()
                .result(result)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<BomResponse> getById(@PathVariable Long id) {
        return ApiResponse.<BomResponse>builder()
                .result(bomService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<BomResponse> update(@PathVariable Long id, @RequestBody @Valid BomRequest request) {
        return ApiResponse.<BomResponse>builder()
                .result(bomService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bomService.delete(id);
        return ApiResponse.<Void>builder().build();
    }
}
