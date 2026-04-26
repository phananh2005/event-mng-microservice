package com.sa.checkinservice.model.entity;

import com.sa.checkinservice.model.enums.CheckinResult;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "checkin_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class CheckinLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_code", nullable = false, length = 100)
    private String ticketCode;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "scanned_by", nullable = false)
    private Long scannedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckinResult result;

    @Column(length = 255)
    private String message;

    @CreatedDate
    @Column(name = "scanned_at", updatable = false)
    private LocalDateTime scannedAt;
}
