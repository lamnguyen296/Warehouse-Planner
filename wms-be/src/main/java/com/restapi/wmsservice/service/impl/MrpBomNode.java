package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Value object biểu diễn một node trong kết quả BOM explosion.
 * Dùng nội bộ trong PlanningEngineService để tránh giữ reference đến entity.
 */
@Getter
@AllArgsConstructor
class MrpBomNode {
    /** ID của component item */
    final Long itemId;
    /** Type của component item */
    final ItemType itemType;
    /** Tổng số lượng cần (đã tính theo cây và số SET yêu cầu) */
    final int requiredQty;
}
