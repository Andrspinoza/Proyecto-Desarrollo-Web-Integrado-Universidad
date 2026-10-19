package com.utp.quesale.moderation.application;

import com.utp.quesale.moderation.application.handlers.queries.GetReportQueryHandler;
import com.utp.quesale.moderation.application.handlers.queries.ListBlocksQueryHandler;
import com.utp.quesale.moderation.application.handlers.queries.ListReportsQueryHandler;
import com.utp.quesale.moderation.application.queries.GetReportQuery;
import com.utp.quesale.moderation.application.queries.ListBlocksQuery;
import com.utp.quesale.moderation.application.queries.ListReportsQuery;
import com.utp.quesale.moderation.domain.aggregates.Block;
import com.utp.quesale.moderation.domain.aggregates.Report;
import com.utp.quesale.moderation.domain.exceptions.ReportNotFoundException;
import com.utp.quesale.moderation.domain.valueobjects.ReportReason;
import com.utp.quesale.moderation.domain.valueobjects.ReportStatus;
import com.utp.quesale.moderation.domain.valueobjects.ReportTargetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModerationQueryHandlersTest {

    private final InMemoryReportRepository reportRepository = new InMemoryReportRepository();
    private final InMemoryBlockRepository blockRepository = new InMemoryBlockRepository();
    private final GetReportQueryHandler getReportHandler = new GetReportQueryHandler(reportRepository);
    private final ListReportsQueryHandler listReportsHandler = new ListReportsQueryHandler(reportRepository);
    private final ListBlocksQueryHandler listBlocksHandler = new ListBlocksQueryHandler(blockRepository);

    @Test
    @DisplayName("Obtiene el DTO de un reporte por id")
    void getsReportById() {
        Report saved = reportRepository.save(new Report(
                12L, ReportTargetType.STUDENT, 34L, ReportReason.HARASSMENT, "Detalle"));

        assertEquals(saved.getId(), getReportHandler.handle(new GetReportQuery(saved.getId())).id());
    }

    @Test
    @DisplayName("Lanza ReportNotFoundException si el reporte no existe")
    void failsToGetMissingReport() {
        assertThrows(ReportNotFoundException.class,
                () -> getReportHandler.handle(new GetReportQuery(404L)));
    }

    @Test
    @DisplayName("Lista todos los reportes cuando no se indica estado")
    void listsAllReports() {
        reportRepository.save(new Report(12L, ReportTargetType.STUDENT, 34L, ReportReason.HARASSMENT, "Uno"));
        reportRepository.save(new Report(13L, ReportTargetType.MESSAGE, 35L, ReportReason.SPAM, "Dos"));

        assertEquals(2, listReportsHandler.handle(new ListReportsQuery(null)).size());
    }

    @Test
    @DisplayName("Filtra reportes por estado")
    void filtersReportsByStatus() {
        Report open = reportRepository.save(new Report(
                12L, ReportTargetType.STUDENT, 34L, ReportReason.HARASSMENT, "Abierto"));
        Report reviewed = reportRepository.save(new Report(
                13L, ReportTargetType.MESSAGE, 35L, ReportReason.SPAM, "Revisado"));
        reviewed.review(99L, "Atendido");
        reportRepository.update(reviewed);

        var results = listReportsHandler.handle(new ListReportsQuery(ReportStatus.OPEN));

        assertEquals(1, results.size());
        assertEquals(open.getId(), results.getFirst().id());
    }

    @Test
    @DisplayName("Lista únicamente los bloqueos del estudiante solicitado")
    void listsBlocksByBlocker() {
        blockRepository.save(new Block(12L, 34L));
        blockRepository.save(new Block(12L, 35L));
        blockRepository.save(new Block(13L, 36L));

        var results = listBlocksHandler.handle(new ListBlocksQuery(12L));

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(block -> block.blockerId().equals(12L)));
    }
}
