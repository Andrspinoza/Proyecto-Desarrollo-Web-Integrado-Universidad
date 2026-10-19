package com.utp.quesale.moderation.infrastructure;

import com.utp.quesale.moderation.domain.aggregates.Report;
import com.utp.quesale.moderation.domain.exceptions.ReportNotFoundException;
import com.utp.quesale.moderation.domain.valueobjects.ReportReason;
import com.utp.quesale.moderation.domain.valueobjects.ReportStatus;
import com.utp.quesale.moderation.domain.valueobjects.ReportTargetType;
import com.utp.quesale.moderation.infrastructure.persistence.jpa.adapters.ReportRepositoryAdapter;
import com.utp.quesale.moderation.infrastructure.persistence.jpa.mappers.ReportPersistenceMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import({ReportRepositoryAdapter.class, ReportPersistenceMapper.class})
class ReportRepositoryAdapterTest {

    @Autowired
    private ReportRepositoryAdapter reportRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesReadsAndFiltersReportsByStatus() {
        ModerationPersistenceTestData.StudentIds students =
                ModerationPersistenceTestData.createStudents(entityManager);
        Report report = reportRepository.save(new Report(
                students.first(), ReportTargetType.STUDENT, students.second(), ReportReason.HARASSMENT,
                "Repeated unwanted messages"));

        Report found = reportRepository.findById(report.getId()).orElseThrow();

        assertEquals(students.first(), found.getReporterId());
        assertEquals(ReportTargetType.STUDENT, found.getTargetType());
        assertEquals(students.second(), found.getTargetId());
        assertEquals(ReportStatus.OPEN, found.getStatus());
        assertEquals("Repeated unwanted messages", found.getDetail());
        assertEquals(1, reportRepository.findAllByStatus(ReportStatus.OPEN).size());
        assertEquals(0, reportRepository.findAllByStatus(ReportStatus.REVIEWED).size());
    }

    @Test
    void updatesReportResolution() {
        Long reporterId = ModerationPersistenceTestData.createStudents(entityManager).first();
        Report saved = reportRepository.save(
                new Report(reporterId, ReportTargetType.MESSAGE, 55L, ReportReason.SPAM, null));
        saved.review(7L, "Reviewed by moderator");

        Report updated = reportRepository.update(saved);

        assertEquals(ReportStatus.REVIEWED, updated.getStatus());
        assertEquals(7L, updated.getReviewedByAccountId());
        assertEquals("Reviewed by moderator", updated.getResolution());
    }

    @Test
    void rejectsUpdatingANonexistentReport() {
        Report unsaved = new Report(1L, ReportTargetType.MESSAGE, 55L, ReportReason.SPAM, null);

        assertThrows(ReportNotFoundException.class, () -> reportRepository.update(unsaved));
    }
}
