package com.synrixa.meetingassistant.repository;

import com.synrixa.meetingassistant.entity.ActionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActionItemRepository extends JpaRepository<ActionItem, UUID> {
    List<ActionItem> findByMeetingIdOrderByCreatedAtAsc(UUID meetingId);
}
