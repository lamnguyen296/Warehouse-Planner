package com.restapi.wmsservice.monitoring;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class WmsOperationMetricsAspect {

    private final WmsMetrics metrics;

    @Around("execution(* com.restapi.wmsservice.service.impl.PlanningServiceImpl.runPlanningEngine(..))")
    public Object observePlanningRun(ProceedingJoinPoint joinPoint) throws Throwable {
        return observe(joinPoint, "planning_run");
    }

    @Around("execution(* com.restapi.wmsservice.service.impl.InventoryOperationsServiceImpl.reserveInventory(..))")
    public Object observeInventoryReservation(ProceedingJoinPoint joinPoint) throws Throwable {
        return observe(joinPoint, "inventory_reserve");
    }

    @Around("execution(* com.restapi.wmsservice.service.impl.InventoryOperationsServiceImpl.receiveGoods(..))")
    public Object observePurchaseReceipt(ProceedingJoinPoint joinPoint) throws Throwable {
        return observe(joinPoint, "purchase_receive");
    }

    @Around("execution(* com.restapi.wmsservice.service.impl.InventoryOperationsServiceImpl.completeRecycle(..))")
    public Object observeRecycleCompletion(ProceedingJoinPoint joinPoint) throws Throwable {
        return observe(joinPoint, "recycle_complete");
    }

    @Around("execution(* com.restapi.wmsservice.service.impl.InventoryOperationsServiceImpl.completeAssembly(..))")
    public Object observeAssemblyCompletion(ProceedingJoinPoint joinPoint) throws Throwable {
        return observe(joinPoint, "assembly_complete");
    }

    @Around("execution(* com.restapi.wmsservice.service.impl.TransferOrderServiceImpl.executeTransfer(..))")
    public Object observeTransferExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        return observe(joinPoint, "transfer_execute");
    }

    @Around("execution(* com.restapi.wmsservice.service.impl.TransferOrderServiceImpl.completeTransfer(..))")
    public Object observeTransferCompletion(ProceedingJoinPoint joinPoint) throws Throwable {
        return observe(joinPoint, "transfer_complete");
    }

    @Around("execution(* com.restapi.wmsservice.service.impl.CloudinaryMediaStorageService.uploadImage(..))")
    public Object observeMediaUpload(ProceedingJoinPoint joinPoint) throws Throwable {
        return observe(joinPoint, "media_upload");
    }

    private Object observe(ProceedingJoinPoint joinPoint, String operation) throws Throwable {
        Timer.Sample sample = metrics.startOperation();
        String outcome = "success";
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            outcome = "failure";
            throw throwable;
        } finally {
            metrics.completeOperation(sample, operation, outcome);
        }
    }
}
