package com.sa.checkinservice.service;

import com.sa.checkinservice.client.TicketServiceClient;
import com.sa.checkinservice.dto.request.ScanRequest;
import com.sa.checkinservice.dto.response.CheckinLogResponse;
import com.sa.checkinservice.mapper.CheckinLogMapper;
import com.sa.checkinservice.model.entity.CheckinLog;
import com.sa.checkinservice.model.enums.CheckinResult;
import com.sa.checkinservice.repository.CheckinLogRepository;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CheckinService {

    CheckinLogRepository checkinLogRepository;
    TicketServiceClient ticketServiceClient;
    CheckinLogMapper checkinLogMapper;

    public CheckinLogResponse scan(ScanRequest request) {
        Long scannedBy = extractUserId();
        CheckinResult result;
        String message;

        try {
            ticketServiceClient.checkIn(request.getTicketCode());
            result = CheckinResult.SUCCESS;
            message = "Check-in successful";
        } catch (FeignException.Conflict e) {
            result = CheckinResult.ALREADY_USED;
            message = "Ticket has already been used";
        } catch (FeignException.BadRequest e) {
            result = CheckinResult.INVALID;
            message = "Ticket is invalid";
        } catch (FeignException.NotFound e) {
            result = CheckinResult.NOT_FOUND;
            message = "Ticket not found";
        } catch (Exception e) {
            log.error("Unexpected error during check-in for code={}", request.getTicketCode(), e);
            result = CheckinResult.INVALID;
            message = e.getMessage();
        }

        CheckinLog log = checkinLogRepository.save(CheckinLog.builder()
                .ticketCode(request.getTicketCode())
                .eventId(request.getEventId())
                .scannedBy(scannedBy)
                .result(result)
                .message(message)
                .build());

        return checkinLogMapper.toResponse(log);
    }

    public List<CheckinLogResponse> getLogsByEvent(Long eventId) {
        return checkinLogRepository.findByEventIdOrderByScannedAtDesc(eventId)
                .stream().map(checkinLogMapper::toResponse).toList();
    }

    private Long extractUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(jwt.getSubject());
    }
}
