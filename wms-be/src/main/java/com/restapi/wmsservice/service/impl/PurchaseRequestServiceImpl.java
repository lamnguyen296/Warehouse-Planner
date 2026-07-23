package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.request.PurchaseRequestDetailRequest;
import com.restapi.wmsservice.dto.request.PurchaseRequestRequest;
import com.restapi.wmsservice.dto.response.PurchaseRequestResponse;
import com.restapi.wmsservice.entity.Item;
import com.restapi.wmsservice.entity.PlanningDetail;
import com.restapi.wmsservice.entity.PurchaseRequest;
import com.restapi.wmsservice.entity.PurchaseRequestDetail;
import com.restapi.wmsservice.enums.PurchaseStatus;
import com.restapi.wmsservice.enums.NotificationType;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.PurchaseRequestDetailMapper;
import com.restapi.wmsservice.mapper.PurchaseRequestMapper;
import com.restapi.wmsservice.repository.ItemRepository;
import com.restapi.wmsservice.repository.PlanningDetailRepository;
import com.restapi.wmsservice.repository.PurchaseRequestRepository;
import com.restapi.wmsservice.service.PurchaseRequestService;
import com.restapi.wmsservice.service.NotificationEventPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PurchaseRequestServiceImpl implements PurchaseRequestService {

    PurchaseRequestRepository purchaseRequestRepository;
    PlanningDetailRepository planningDetailRepository;
    ItemRepository itemRepository;
    PurchaseRequestMapper purchaseRequestMapper;
    PurchaseRequestDetailMapper purchaseRequestDetailMapper;
    NotificationEventPublisher notificationEventPublisher;

    @Override
    @Transactional
    public PurchaseRequestResponse create(PurchaseRequestRequest request) {
        PlanningDetail planningDetail = null;
        if (request.getPlanningDetailId() != null) {
            planningDetail = planningDetailRepository.findByIdForUpdate(request.getPlanningDetailId())
                    .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));
            validatePlannedQuantity(planningDetail, request, null);
        }

        PurchaseRequest purchaseRequest = purchaseRequestMapper.toEntity(request);
        
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        purchaseRequest.setRequestNo("PR-" + dateStr + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        
        if (planningDetail != null) {
            purchaseRequest.setPlanningDetail(planningDetail);
        }
        purchaseRequest.setStatus(PurchaseStatus.PENDING);

        if (request.getDetails() != null) {
            for (PurchaseRequestDetailRequest detailReq : request.getDetails()) {
                Item item = itemRepository.findById(detailReq.getItemId())
                        .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
                validatePlanningItem(planningDetail, item);

                PurchaseRequestDetail detail = purchaseRequestDetailMapper.toEntity(detailReq);
                detail.setItem(item);
                detail.setPurchaseRequest(purchaseRequest);
                detail.setReceivedQuantity(0);
                
                purchaseRequest.getDetails().add(detail);
            }
        }

        return purchaseRequestMapper.toResponse(purchaseRequestRepository.save(purchaseRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseRequestResponse get(Long id) {
        return purchaseRequestRepository.findById(id)
                .map(purchaseRequestMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.PURCHASE_REQUEST_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseRequestResponse> getAll() {
        return purchaseRequestRepository.findAll().stream()
                .map(purchaseRequestMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PurchaseRequestResponse update(Long id, PurchaseRequestRequest request) {
        PurchaseRequest purchaseRequest = purchaseRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.PURCHASE_REQUEST_NOT_FOUND));

        if (purchaseRequest.getStatus() != PurchaseStatus.PENDING) {
            throw new AppException(ErrorCode.PURCHASE_REQUEST_INVALID_STATUS);
        }
        validatePlanningLink(purchaseRequest.getPlanningDetail(), request.getPlanningDetailId());

        PlanningDetail planningDetail = null;
        if (request.getPlanningDetailId() != null) {
            planningDetail = planningDetailRepository.findByIdForUpdate(request.getPlanningDetailId())
                    .orElseThrow(() -> new AppException(ErrorCode.PLANNING_DETAIL_NOT_FOUND));
            validatePlannedQuantity(planningDetail, request, id);
        }

        purchaseRequestMapper.updateEntity(purchaseRequest, request);
        
        if (planningDetail != null) {
            purchaseRequest.setPlanningDetail(planningDetail);
        } else {
            purchaseRequest.setPlanningDetail(null);
        }

        purchaseRequest.getDetails().clear();
        if (request.getDetails() != null) {
            for (PurchaseRequestDetailRequest detailReq : request.getDetails()) {
                Item item = itemRepository.findById(detailReq.getItemId())
                        .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
                validatePlanningItem(planningDetail, item);

                PurchaseRequestDetail detail = purchaseRequestDetailMapper.toEntity(detailReq);
                detail.setItem(item);
                detail.setPurchaseRequest(purchaseRequest);
                detail.setReceivedQuantity(0);
                
                purchaseRequest.getDetails().add(detail);
            }
        }

        return purchaseRequestMapper.toResponse(purchaseRequestRepository.save(purchaseRequest));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PurchaseRequest purchaseRequest = purchaseRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.PURCHASE_REQUEST_NOT_FOUND));
        if (purchaseRequest.getStatus() != PurchaseStatus.PENDING) {
            throw new AppException(ErrorCode.PURCHASE_REQUEST_INVALID_STATUS);
        }
        purchaseRequestRepository.deleteById(id);
    }

    // ── Business Flow (Phase 6) ───────────────────────────────────────────

    @Override
    @Transactional
    public PurchaseRequestResponse approvePurchase(Long id) {
        PurchaseRequest purchaseRequest = purchaseRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.PURCHASE_REQUEST_NOT_FOUND));

        if (purchaseRequest.getStatus() != PurchaseStatus.PENDING) {
            throw new AppException(ErrorCode.PURCHASE_REQUEST_INVALID_STATUS);
        }

        purchaseRequest.setStatus(PurchaseStatus.APPROVED);
        purchaseRequest = purchaseRequestRepository.save(purchaseRequest);
        log.info("PurchaseRequest approved [id={}, no={}]", purchaseRequest.getId(), purchaseRequest.getRequestNo());
        publishPurchaseEvent(purchaseRequest, NotificationType.PURCHASE_APPROVED,
                "Purchase approved", " was approved.");
        return purchaseRequestMapper.toResponse(purchaseRequest);
    }

    @Override
    @Transactional
    public PurchaseRequestResponse orderPurchase(Long id) {
        PurchaseRequest purchaseRequest = purchaseRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.PURCHASE_REQUEST_NOT_FOUND));

        if (purchaseRequest.getStatus() != PurchaseStatus.APPROVED) {
            throw new AppException(ErrorCode.PURCHASE_REQUEST_INVALID_STATUS);
        }

        purchaseRequest.setStatus(PurchaseStatus.ORDERED);
        purchaseRequest = purchaseRequestRepository.save(purchaseRequest);
        log.info("PurchaseRequest ordered [id={}, no={}]", purchaseRequest.getId(), purchaseRequest.getRequestNo());
        publishPurchaseEvent(purchaseRequest, NotificationType.PURCHASE_ORDERED,
                "Purchase ordered", " was sent to the supplier.");
        return purchaseRequestMapper.toResponse(purchaseRequest);
    }

    @Override
    @Transactional
    public PurchaseRequestResponse cancelPurchase(Long id) {
        PurchaseRequest purchaseRequest = purchaseRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.PURCHASE_REQUEST_NOT_FOUND));

        if (purchaseRequest.getStatus() != PurchaseStatus.PENDING && purchaseRequest.getStatus() != PurchaseStatus.APPROVED) {
            throw new AppException(ErrorCode.PURCHASE_REQUEST_INVALID_STATUS);
        }

        purchaseRequest.setStatus(PurchaseStatus.CANCELLED);
        purchaseRequest = purchaseRequestRepository.save(purchaseRequest);
        log.info("PurchaseRequest cancelled [id={}, no={}]", purchaseRequest.getId(), purchaseRequest.getRequestNo());
        publishPurchaseEvent(purchaseRequest, NotificationType.PURCHASE_CANCELLED,
                "Purchase cancelled", " was cancelled.");
        return purchaseRequestMapper.toResponse(purchaseRequest);
    }

    private void publishPurchaseEvent(PurchaseRequest request, NotificationType type,
                                      String title, String messageSuffix) {
        notificationEventPublisher.publish(type, title, request.getRequestNo() + messageSuffix,
                "PurchaseRequest", request.getId(), planningRequester(request.getPlanningDetail()),
                Set.of(com.restapi.wmsservice.security.PermissionCode.PURCHASE_READ));
    }

    private Set<String> planningRequester(PlanningDetail detail) {
        if (detail == null || detail.getPlanning() == null
                || detail.getPlanning().getWorkshopRequest() == null) {
            return Set.of();
        }
        String username = detail.getPlanning().getWorkshopRequest().getCreatedBy();
        return username == null || username.isBlank() ? Set.of() : Set.of(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseRequestResponse> getByStatus(PurchaseStatus status) {
        // Will need to add findByStatus to repository
        return purchaseRequestRepository.findByStatus(status).stream()
                .map(purchaseRequestMapper::toResponse)
                .toList();
    }

    private void validatePlanningItem(PlanningDetail planningDetail, Item item) {
        if (planningDetail != null && !planningDetail.getItem().getId().equals(item.getId())) {
            throw new AppException(ErrorCode.ORDER_PLANNING_ITEM_MISMATCH);
        }
    }

    private void validatePlanningLink(PlanningDetail currentPlanningDetail, Long requestedPlanningDetailId) {
        if (currentPlanningDetail != null
                && !currentPlanningDetail.getId().equals(requestedPlanningDetailId)) {
            throw new AppException(ErrorCode.ORDER_PLANNING_ITEM_MISMATCH);
        }
    }

    private void validatePlannedQuantity(PlanningDetail planningDetail,
                                         PurchaseRequestRequest request,
                                         Long excludedRequestId) {
        if (planningDetail.getPlanning().getStatus() != com.restapi.wmsservice.enums.PlanningStatus.EXECUTING) {
            throw new AppException(ErrorCode.PLANNING_INVALID_STATUS);
        }
        int requestedQuantity = request.getDetails().stream()
                .mapToInt(com.restapi.wmsservice.dto.request.PurchaseRequestDetailRequest::getQuantity)
                .sum();
        int committedQuantity = purchaseRequestRepository.findByPlanningDetailId(planningDetail.getId()).stream()
                .filter(existing -> !existing.getId().equals(excludedRequestId))
                .filter(existing -> existing.getStatus() != PurchaseStatus.CANCELLED)
                .flatMap(existing -> existing.getDetails().stream())
                .mapToInt(PurchaseRequestDetail::getQuantity)
                .sum();
        if ((long) committedQuantity + requestedQuantity > planningDetail.getPurchaseQuantity()) {
            throw new AppException(ErrorCode.PLANNING_ORDER_QUANTITY_EXCEEDED);
        }
    }
}
