package com.synrixa.meetingassistant.repository;

import com.synrixa.meetingassistant.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SummaryRepository extends JpaRepository<Summary, UUID> {
    Optional<Summary> findByMeetingId(UUID meetingId);
}
