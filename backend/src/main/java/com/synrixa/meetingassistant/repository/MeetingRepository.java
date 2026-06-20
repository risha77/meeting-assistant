package com.synrixa.meetingassistant.repository;

import com.synrixa.meetingassistant.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
    List<Meeting> findAllByOrderByStartedAtDesc();
    List<Meeting> findByStatusOrderByStartedAtDesc(Meeting.MeetingStatus status);
}
