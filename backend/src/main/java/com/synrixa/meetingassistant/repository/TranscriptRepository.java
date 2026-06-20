package com.synrixa.meetingassistant.repository;

import com.synrixa.meetingassistant.entity.Transcript;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TranscriptRepository extends JpaRepository<Transcript, UUID> {

    List<Transcript> findByMeetingIdOrderByTsAsc(UUID meetingId);

    /**
     * Full-text search across all meetings.
     * JOIN FETCH meeting so title is available without lazy-load after the transaction closes.
     * Pageable limits results to 50 — avoids non-standard JPQL LIMIT clause.
     */
    @Query("""
        SELECT t FROM Transcript t
        JOIN FETCH t.meeting m
        WHERE LOWER(t.content) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY t.ts DESC
        """)
    List<Transcript> searchByContent(@Param("query") String query, Pageable pageable);

    /**
     * Search within a single meeting.
     */
    @Query("""
        SELECT t FROM Transcript t
        JOIN FETCH t.meeting m
        WHERE t.meeting.id = :meetingId
        AND LOWER(t.content) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY t.ts ASC
        """)
    List<Transcript> searchInMeeting(@Param("meetingId") UUID meetingId,
                                     @Param("query") String query);

    /**
     * Concatenate all transcript turns for a meeting into a single string for OpenAI.
     */
    @Query(value = """
        SELECT string_agg(t.content, ' ' ORDER BY t.ts ASC)
        FROM transcripts t
        WHERE t.meeting_id = :meetingId
        """, nativeQuery = true)
    String getFullTranscript(@Param("meetingId") UUID meetingId);
}
