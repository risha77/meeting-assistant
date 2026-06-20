package com.synrixa.meetingassistant.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transcripts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transcript {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column
    private String speaker;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "ts", nullable = false)
    private LocalDateTime ts;

    @PrePersist
    protected void onCreate() {
        if (ts == null) ts = LocalDateTime.now();
    }
}
