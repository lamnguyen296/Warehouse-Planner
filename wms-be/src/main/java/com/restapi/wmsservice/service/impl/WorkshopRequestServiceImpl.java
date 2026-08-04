package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.request.WorkshopRequestDetailRequest;
import com.restapi.wmsservice.dto.request.WorkshopRequestRequest;
import com.restapi.wmsservice.dto.response.WorkshopRequestResponse;
import com.restapi.wmsservice.entity.Item;
import com.restapi.wmsservice.entity.WorkshopRequest;
import com.restapi.wmsservice.entity.WorkshopRequestDetail;
import com.restapi.wmsservice.enums.ItemType;
import com.restapi.wmsservice.enums.NotificationType;
import com.restapi.wmsservice.enums.RequestStatus;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.WorkshopRequestDetailMapper;
import com.restapi.wmsservice.mapper.WorkshopRequestMapper;
import com.restapi.wmsservice.repository.ItemRepository;
import com.restapi.wmsservice.repository.WorkshopRequestRepository;
import com.restapi.wmsservice.service.WorkshopRequestService;
import com.restapi.wmsservice.service.NotificationEventPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkshopRequestServiceImpl implements WorkshopRequestService {

    WorkshopRequestRepository workshopRequestRepository;
    ItemRepository itemRepository;
    WorkshopRequestMapper workshopRequestMapper;
    WorkshopRequestDetailMapper workshopRequestDetailMapper;
    NotificationEventPublisher notificationEventPublisher;

    // ── CRUD ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public WorkshopRequestResponse createRequest(WorkshopRequestRequest request) {
        // 1. Validate date constraint
        validateDates(request.getRequestedDate(), request.getExpectedDate());

        // 2. Build entity skeleton (no status / requestNo yet)
        WorkshopRequest workshopRequest = workshopRequestMapper.toRequest(request);

        // 3. Set system-controlled fields
        workshopRequest.setStatus(RequestStatus.DRAFT);
        workshopRequest.setRequestNo(generateRequestNo());
        workshopRequest.setCreatedBy(currentUsername());
        workshopRequest.setRequestedDate(
                request.getRequestedDate() != null ? request.getRequestedDate() : LocalDateTime.now()
        );

        // 4. Build and validate details
        List<WorkshopRequestDetail> details = buildAndValidateDetails(request, workshopRequest);
        workshopRequest.setDetails(details);

        workshopRequest = workshopRequestRepository.save(workshopRequest);
        log.info("Created WorkshopRequest [id={}, no={}]", workshopRequest.getId(), workshopRequest.getRequestNo());
        return mapToResponse(workshopRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkshopRequestResponse getRequest(Long id) {
        WorkshopRequest request = findOrThrow(id);
        validateWorkshopOwnership(request);
        return mapToResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkshopRequestResponse> getAllRequests() {
        List<WorkshopRequest> requests = canReadAllWorkshopRequests()
                ? workshopRequestRepository.findAll()
                : workshopRequestRepository.findByCreatedBy(currentUsername());
        return requests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkshopRequestResponse updateRequest(Long id, WorkshopRequestRequest request) {
        WorkshopRequest workshopRequest = workshopRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_FOUND));
        validateWorkshopOwnership(workshopRequest);

        // Chỉ cho phép cập nhật khi còn ở trạng thái DRAFT
        if (workshopRequest.getStatus() != RequestStatus.DRAFT) {
            throw new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_EDITABLE);
        }

        // Validate date constraint
        validateDates(request.getRequestedDate(), request.getExpectedDate());

        workshopRequestMapper.updateRequest(workshopRequest, request);

        // Ghi đè requestedDate nếu client gửi, hoặc giữ nguyên
        if (request.getRequestedDate() != null) {
            workshopRequest.setRequestedDate(request.getRequestedDate());
        }

        // Rebuild details
        workshopRequest.getDetails().clear();
        List<WorkshopRequestDetail> details = buildAndValidateDetails(request, workshopRequest);
        workshopRequest.getDetails().addAll(details);

        workshopRequest = workshopRequestRepository.save(workshopRequest);
        log.info("Updated WorkshopRequest [id={}, no={}]", workshopRequest.getId(), workshopRequest.getRequestNo());
        return mapToResponse(workshopRequest);
    }

    @Override
    @Transactional
    public void deleteRequest(Long id) {
        WorkshopRequest workshopRequest = workshopRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_FOUND));
        validateWorkshopOwnership(workshopRequest);

        // Chỉ xóa được khi ở trạng thái DRAFT
        if (workshopRequest.getStatus() != RequestStatus.DRAFT) {
            throw new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_EDITABLE);
        }

        workshopRequestRepository.deleteById(id);
        log.info("Deleted WorkshopRequest [id={}]", id);
    }

    // ── Business Flow (Phase 6) ───────────────────────────────────────────

    @Override
    @Transactional
    public WorkshopRequestResponse submitRequest(Long id) {
        WorkshopRequest workshopRequest = workshopRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_FOUND));
        validateWorkshopOwnership(workshopRequest);

        // Guard: chỉ DRAFT mới submit được
        if (workshopRequest.getStatus() == RequestStatus.SUBMITTED) {
            throw new AppException(ErrorCode.WORKSHOP_REQUEST_ALREADY_SUBMITTED);
        }
        if (workshopRequest.getStatus() != RequestStatus.DRAFT) {
            throw new AppException(ErrorCode.INVALID_REQUEST_STATUS_TRANSITION);
        }

        // Business rule: phải có ít nhất 1 detail khi submit
        if (workshopRequest.getDetails() == null || workshopRequest.getDetails().isEmpty()) {
            throw new AppException(ErrorCode.DETAILS_REQUIRED_FOR_SUBMIT);
        }

        workshopRequest.setStatus(RequestStatus.SUBMITTED);
        workshopRequest = workshopRequestRepository.save(workshopRequest);
        log.info("Submitted WorkshopRequest [id={}, no={}]", workshopRequest.getId(), workshopRequest.getRequestNo());
        notificationEventPublisher.publish(NotificationType.WORKSHOP_REQUEST_SUBMITTED,
                "Workshop request submitted",
                workshopRequest.getRequestNo() + " is waiting for approval.",
                "WorkshopRequest", workshopRequest.getId(), Set.of(),
                Set.of(com.restapi.wmsservice.security.PermissionCode.WORKSHOP_REQUEST_APPROVE));
        return mapToResponse(workshopRequest);
    }

    @Override
    @Transactional
    public WorkshopRequestResponse approveRequest(Long id) {
        WorkshopRequest workshopRequest = workshopRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_FOUND));

        // Guard: chỉ SUBMITTED mới approve được
        if (workshopRequest.getStatus() != RequestStatus.SUBMITTED) {
            throw new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_SUBMITTED);
        }

        workshopRequest.setStatus(RequestStatus.APPROVED);
        workshopRequest = workshopRequestRepository.save(workshopRequest);
        log.info("Approved WorkshopRequest [id={}, no={}]", workshopRequest.getId(), workshopRequest.getRequestNo());
        notificationEventPublisher.publish(NotificationType.WORKSHOP_REQUEST_APPROVED,
                "Workshop request approved",
                workshopRequest.getRequestNo() + " is ready for planning.",
                "WorkshopRequest", workshopRequest.getId(), Set.of(workshopRequest.getCreatedBy()),
                Set.of(com.restapi.wmsservice.security.PermissionCode.PLANNING_RUN));
        return mapToResponse(workshopRequest);
    }

    @Override
    @Transactional
    public WorkshopRequestResponse cancelRequest(Long id) {
        WorkshopRequest workshopRequest = workshopRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_FOUND));
        validateWorkshopOwnership(workshopRequest);

        // Chỉ DRAFT và SUBMITTED mới cancel được
        if (workshopRequest.getStatus() != RequestStatus.DRAFT
                && workshopRequest.getStatus() != RequestStatus.SUBMITTED) {
            throw new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_CANCELLABLE);
        }

        // Không được cancel nếu có Planning đang active
        long activePlannings = workshopRequestRepository.countActivePlanningsByRequestId(id);
        if (activePlannings > 0) {
            throw new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_CANCELLABLE);
        }

        workshopRequest.setStatus(RequestStatus.CANCELLED);
        workshopRequest = workshopRequestRepository.save(workshopRequest);
        log.info("Cancelled WorkshopRequest [id={}, no={}]", workshopRequest.getId(), workshopRequest.getRequestNo());
        notificationEventPublisher.publish(NotificationType.WORKSHOP_REQUEST_CANCELLED,
                "Workshop request cancelled",
                workshopRequest.getRequestNo() + " was cancelled.",
                "WorkshopRequest", workshopRequest.getId(), Set.of(workshopRequest.getCreatedBy()), Set.of());
        return mapToResponse(workshopRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkshopRequestResponse> getByStatus(RequestStatus status) {
        return workshopRequestRepository.findByStatus(status).stream()
                .filter(request -> canReadAllWorkshopRequests()
                        || Objects.equals(request.getCreatedBy(), currentUsername()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Tìm entity hoặc throw WORKSHOP_REQUEST_NOT_FOUND.
     * Repository dùng @EntityGraph nên details và item được load cùng query.
     */
    private WorkshopRequest findOrThrow(Long id) {
        return workshopRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSHOP_REQUEST_NOT_FOUND));
    }

    private String currentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }

    private boolean canReadAllWorkshopRequests() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "WORKSHOP_REQUEST_READ_ALL".equals(authority.getAuthority()));
    }

    private void validateWorkshopOwnership(WorkshopRequest request) {
        if (!canReadAllWorkshopRequests() && !currentUsername().equals(request.getCreatedBy())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * Validate date: nếu cả hai đều có → expectedDate phải sau requestedDate.
     */
    private void validateDates(LocalDateTime requestedDate, LocalDateTime expectedDate) {
        if (requestedDate != null && expectedDate != null) {
            if (!expectedDate.isAfter(requestedDate)) {
                throw new AppException(ErrorCode.INVALID_EXPECTED_DATE);
            }
        }
    }

    /**
     * Build WorkshopRequestDetail list với validation:
     * - itemType phải là SET
     * - quantity > 0 (đã validate ở DTO layer, guard lại ở đây)
     * - không được trùng itemId trong cùng 1 request
     */
    private List<WorkshopRequestDetail> buildAndValidateDetails(
            WorkshopRequestRequest request, WorkshopRequest parent) {

        Set<Long> itemIds = request.getDetails().stream()
                .map(WorkshopRequestDetailRequest::getItemId)
                .collect(Collectors.toSet());

        // Guard: duplicate item
        if (itemIds.size() < request.getDetails().size()) {
            throw new AppException(ErrorCode.DUPLICATE_ITEM_IN_REQUEST);
        }

        // N+1-safe: batch fetch all items
        Map<Long, Item> itemMap = itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, item -> item));

        return request.getDetails().stream().map(detailRequest -> {
            // Guard: quantity > 0
            if (detailRequest.getQuantity() == null || detailRequest.getQuantity() <= 0) {
                throw new AppException(ErrorCode.INVALID_QUANTITY);
            }

            Item item = itemMap.get(detailRequest.getItemId());
            if (item == null) {
                throw new AppException(ErrorCode.ITEM_NOT_FOUND);
            }

            // Business rule: chỉ cho phép item loại SET trong Workshop Request
            if (item.getItemType() != ItemType.SET) {
                throw new AppException(ErrorCode.INVALID_ITEM_TYPE);
            }
            if (item.getStatus() != com.restapi.wmsservice.enums.ItemStatus.ACTIVE) {
                throw new AppException(ErrorCode.ITEM_NOT_ACTIVE);
            }

            WorkshopRequestDetail detail = workshopRequestDetailMapper.toDetail(detailRequest);
            detail.setItem(item);
            detail.setWorkshopRequest(parent);
            return detail;
        }).collect(Collectors.toList());
    }

    /**
     * Sinh requestNo theo format WR-YYYYMMDD-XXXX.
     * Dùng UUID 4-char suffix. Retry tối đa 5 lần nếu trùng.
     */
    private String generateRequestNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        for (int attempt = 0; attempt < 5; attempt++) {
            String suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            String no = "WR-" + dateStr + "-" + suffix;
            if (!workshopRequestRepository.existsByRequestNo(no)) {
                return no;
            }
            log.warn("requestNo collision [no={}], retrying... attempt={}", no, attempt + 1);
        }
        // Fallback: dùng full UUID suffix nếu vẫn collision
        return "WR-" + dateStr + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Map entity sang response DTO.
     * Details được load sẵn từ @EntityGraph → không gây thêm query.
     */
    private WorkshopRequestResponse mapToResponse(WorkshopRequest request) {
        WorkshopRequestResponse response = workshopRequestMapper.toResponse(request);
        if (request.getDetails() != null) {
            response.setDetails(request.getDetails().stream()
                    .map(workshopRequestDetailMapper::toResponse)
                    .collect(Collectors.toList()));
        }
        return response;
    }
}

