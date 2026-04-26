package com.sa.checkinservice.faker;

import com.sa.checkinservice.model.entity.CheckinLog;
import com.sa.checkinservice.model.enums.CheckinResult;
import com.sa.checkinservice.repository.CheckinLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CheckinLogSeeder {

    private final CheckinLogRepository checkinLogRepository;

    public void seed() {
        if (checkinLogRepository.count() > 0) return;
        checkinLogRepository.saveAll(List.of(
                CheckinLog.builder().ticketCode("TK-SAMPLE01").eventId(1L).scannedBy(4L).result(CheckinResult.SUCCESS).message("Check-in successful.").build(),
                CheckinLog.builder().ticketCode("TK-SAMPLE02").eventId(1L).scannedBy(4L).result(CheckinResult.ALREADY_USED).message("Ticket already used.").build(),
                CheckinLog.builder().ticketCode("TK-INVALID").eventId(2L).scannedBy(4L).result(CheckinResult.INVALID).message("Invalid ticket code.").build()
        ));
    }
}
