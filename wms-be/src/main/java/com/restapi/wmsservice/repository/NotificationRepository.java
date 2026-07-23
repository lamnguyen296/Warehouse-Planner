package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop50ByRecipientUsernameOrderByCreatedAtDesc(String recipientUsername);

    long countByRecipientUsernameAndReadAtIsNull(String recipientUsername);

    Optional<Notification> findByIdAndRecipientUsername(Long id, String recipientUsername);

    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :readAt " +
            "WHERE n.recipientUsername = :username AND n.readAt IS NULL")
    int markAllRead(@Param("username") String username, @Param("readAt") LocalDateTime readAt);
}
