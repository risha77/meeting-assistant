package com.synrixa.meetingassistant.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "action_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column
    private String assignee;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String task;

    @Column
    private LocalDate deadline;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = ActionStatus.PENDING;
    }

    public enum ActionStatus {
        PENDING, IN_PROGRESS, DONE
    }
}
