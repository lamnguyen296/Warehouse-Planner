package com.restapi.wmsservice.service;

import com.restapi.wmsservice.entity.Planning;
import com.restapi.wmsservice.entity.PlanningDetail;
import com.restapi.wmsservice.entity.TransferOrder;
import com.restapi.wmsservice.enums.AssemblyStatus;
import com.restapi.wmsservice.enums.ItemType;
import com.restapi.wmsservice.enums.NotificationType;
import com.restapi.wmsservice.enums.PlanningStatus;
import com.restapi.wmsservice.enums.PurchaseStatus;
import com.restapi.wmsservice.enums.RecycleStatus;
import com.restapi.wmsservice.enums.RequestStatus;
import com.restapi.wmsservice.enums.TransferStatus;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.repository.AssemblyOrderRepository;
import com.restapi.wmsservice.repository.PlanningRepository;
import com.restapi.wmsservice.repository.PurchaseRequestRepository;
import com.restapi.wmsservice.repository.RecycleOrderRepository;
import com.restapi.wmsservice.repository.TransferOrderRepository;
import com.restapi.wmsservice.security.PermissionCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlanningCompletionService {

    PlanningRepository planningRepository;
    TransferOrderRepository transferOrderRepository;
    PurchaseRequestRepository purchaseRequestRepository;
    RecycleOrderRepository recycleOrderRepository;
    AssemblyOrderRepository assemblyOrderRepository;
    NotificationEventPublisher notificationEventPublisher;

    @Transactional
    public boolean tryComplete(Long planningId) {
        if (planningId == null) {
            return false;
        }
        Planning planning = planningRepository.findByIdForUpdate(planningId)
                .orElseThrow(() -> new AppException(ErrorCode.PLANNING_NOT_FOUND));
        if (planning.getStatus() != PlanningStatus.EXECUTING
                || !allSetDemandDelivered(planning)
                || hasActiveSupplyOrders(planning)) {
            return false;
        }

        planning.setStatus(PlanningStatus.COMPLETED);
        planning.getWorkshopRequest().setStatus(RequestStatus.COMPLETED);
        planningRepository.save(planning);
        notificationEventPublisher.publish(NotificationType.PLANNING_COMPLETED,
                "Planning completed",
                planning.getPlanningNo() + " and its workshop request are complete.",
                "Planning", planning.getId(), requester(planning),
                Set.of(PermissionCode.PLANNING_READ));
        return true;
    }

    private boolean allSetDemandDelivered(Planning planning) {
        Map<Long, Integer> deliveredByItem = transferOrderRepository.findByPlanningId(planning.getId()).stream()
                .filter(transfer -> transfer.getStatus() == TransferStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                        transfer -> transfer.getItem().getId(),
                        Collectors.summingInt(TransferOrder::getQuantity)));
        return planning.getDetails().stream()
                .filter(detail -> detail.getItem().getItemType() == ItemType.SET)
                .allMatch(detail -> deliveredByItem.getOrDefault(detail.getItem().getId(), 0)
                        >= detail.getRequiredQuantity());
    }

    private boolean hasActiveSupplyOrders(Planning planning) {
        for (PlanningDetail detail : planning.getDetails()) {
            boolean activePurchase = purchaseRequestRepository.findByPlanningDetailId(detail.getId()).stream()
                    .anyMatch(request -> request.getStatus() != PurchaseStatus.RECEIVED
                            && request.getStatus() != PurchaseStatus.CANCELLED);
            boolean activeRecycle = recycleOrderRepository.findByPlanningDetailId(detail.getId()).stream()
                    .anyMatch(order -> order.getStatus() != RecycleStatus.COMPLETED
                            && order.getStatus() != RecycleStatus.FAILED);
            boolean activeAssembly = assemblyOrderRepository.findByPlanningDetailId(detail.getId()).stream()
                    .anyMatch(order -> order.getStatus() != AssemblyStatus.COMPLETED
                            && order.getStatus() != AssemblyStatus.FAILED);
            if (activePurchase || activeRecycle || activeAssembly) {
                return true;
            }
        }
        return false;
    }

    private Set<String> requester(Planning planning) {
        String username = planning.getWorkshopRequest().getCreatedBy();
        return username == null || username.isBlank() ? Set.of() : Set.of(username);
    }
}
