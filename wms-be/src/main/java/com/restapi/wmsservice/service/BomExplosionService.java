package com.restapi.wmsservice.service;

import com.restapi.wmsservice.entity.Bom;
import com.restapi.wmsservice.entity.Item;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.enums.ItemStatus;
import com.restapi.wmsservice.repository.BomRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomExplosionService {

    BomRepository bomRepository;

    public List<ComponentRequirement> explodeLeafComponents(Item parentItem, int parentQuantity) {
        if (parentQuantity <= 0) {
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }
        if (parentItem.getStatus() != ItemStatus.ACTIVE) {
            throw new AppException(ErrorCode.ITEM_NOT_ACTIVE);
        }

        Map<Long, ComponentRequirement> requirements = new LinkedHashMap<>();
        Set<Long> path = new HashSet<>();
        path.add(parentItem.getId());
        explode(parentItem, parentQuantity, requirements, path);
        return List.copyOf(requirements.values());
    }

    private void explode(Item parentItem,
                         int parentQuantity,
                         Map<Long, ComponentRequirement> requirements,
                         Set<Long> path) {
        List<Bom> children = bomRepository.findByParentItemId(parentItem.getId());
        if (children.isEmpty()) {
            requirements.merge(parentItem.getId(),
                    new ComponentRequirement(parentItem, parentQuantity),
                    (current, added) -> new ComponentRequirement(
                            current.item(), addExact(current.quantity(), added.quantity())));
            return;
        }

        for (Bom bom : children) {
            Item childItem = bom.getChildItem();
            if (childItem.getStatus() != ItemStatus.ACTIVE) {
                throw new AppException(ErrorCode.ITEM_NOT_ACTIVE);
            }
            if (!path.add(childItem.getId())) {
                throw new AppException(ErrorCode.BOM_CYCLE_DETECTED);
            }
            explode(childItem, multiplyExact(parentQuantity, bom.getQuantity()), requirements, path);
            path.remove(childItem.getId());
        }
    }

    private int multiplyExact(int left, int right) {
        try {
            return Math.multiplyExact(left, right);
        } catch (ArithmeticException exception) {
            throw new AppException(ErrorCode.PLANNING_QUANTITY_OVERFLOW);
        }
    }

    private int addExact(int left, int right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException exception) {
            throw new AppException(ErrorCode.PLANNING_QUANTITY_OVERFLOW);
        }
    }

    public record ComponentRequirement(Item item, int quantity) {
    }
}
