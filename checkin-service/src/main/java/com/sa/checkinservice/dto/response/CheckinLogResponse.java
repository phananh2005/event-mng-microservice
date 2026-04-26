package com.sa.checkinservice.dto.response;

import com.sa.checkinservice.model.enums.CheckinResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckinLogResponse {
    private Long id;
    private String ticketCode;
    private Long eventId;
    private Long scannedBy;
    private CheckinResult result;
    private String message;
    private LocalDateTime scannedAt;
}
