package com.sa.checkinservice.repository;

import com.sa.checkinservice.model.entity.CheckinLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckinLogRepository extends JpaRepository<CheckinLog, Long> {
    List<CheckinLog> findByEventIdOrderByScannedAtDesc(Long eventId);
}
