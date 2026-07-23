package com.restapi.wmsservice.controller;

import com.restapi.wmsservice.dto.request.ApiResponse;
import com.restapi.wmsservice.dto.response.NotificationResponse;
import com.restapi.wmsservice.dto.response.UnreadNotificationCountResponse;
import com.restapi.wmsservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    ApiResponse<List<NotificationResponse>> getMyNotifications() {
        return ApiResponse.<List<NotificationResponse>>builder()
                .result(notificationService.getMyNotifications())
                .build();
    }

    @GetMapping("/unread-count")
    ApiResponse<UnreadNotificationCountResponse> getMyUnreadCount() {
        return ApiResponse.<UnreadNotificationCountResponse>builder()
                .result(notificationService.getMyUnreadCount())
                .build();
    }

    @PutMapping("/{id}/read")
    ApiResponse<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ApiResponse.<NotificationResponse>builder()
                .result(notificationService.markAsRead(id))
                .build();
    }

    @PutMapping("/read-all")
    ApiResponse<UnreadNotificationCountResponse> markAllAsRead() {
        return ApiResponse.<UnreadNotificationCountResponse>builder()
                .result(notificationService.markAllAsRead())
                .build();
    }
}
