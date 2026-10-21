package com.utp.quesale.moderation.application;

import com.utp.quesale.moderation.application.commands.CreateBlockCommand;
import com.utp.quesale.moderation.application.commands.CreateReportCommand;
import com.utp.quesale.moderation.application.commands.DeleteBlockCommand;
import com.utp.quesale.moderation.application.commands.DismissReportCommand;
import com.utp.quesale.moderation.application.commands.ReviewReportCommand;
import com.utp.quesale.moderation.application.dtos.BlockDto;
import com.utp.quesale.moderation.application.dtos.ReportDto;
import com.utp.quesale.moderation.application.handlers.commands.BlockCommandHandler;
import com.utp.quesale.moderation.application.handlers.commands.ReportCommandHandler;
import com.utp.quesale.moderation.domain.exceptions.BlockNotFoundException;
import com.utp.quesale.moderation.domain.exceptions.ReportNotFoundException;
import com.utp.quesale.moderation.domain.valueobjects.ReportReason;
import com.utp.quesale.moderation.domain.valueobjects.ReportStatus;
import com.utp.quesale.moderation.domain.valueobjects.ReportTargetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModerationCommandHandlersTest {

    private final InMemoryReportRepository reportRepository = new InMemoryReportRepository();
    private final InMemoryBlockRepository blockRepository = new InMemoryBlockRepository();
    private final ReportCommandHandler reportHandler = new ReportCommandHandler(reportRepository);
    private final BlockCommandHandler blockHandler = new BlockCommandHandler(blockRepository);

    @Test
    @DisplayName("Crea un reporte y devuelve su DTO persistido")
    void createsReport() {
        ReportDto dto = reportHandler.handle(new CreateReportCommand(
                12L, ReportTargetType.STUDENT, 34L, ReportReason.HARASSMENT, "Mensajes insistentes"));

        assertEquals(1L, dto.id());
        assertEquals(12L, dto.reporterId());
        assertEquals(ReportStatus.OPEN, dto.status());
        assertEquals("Mensajes insistentes", dto.detail());
    }

    @Test
    @DisplayName("Revisa un reporte abierto y persiste el resultado")
    void reviewsReport() {
        ReportDto created = createReport();

        ReportDto reviewed = reportHandler.handle(new ReviewReportCommand(created.id(), 99L, "Caso atendido"));

        assertEquals(ReportStatus.REVIEWED, reviewed.status());
        assertEquals(99L, reviewed.reviewedByAccountId());
        assertEquals("Caso atendido", reviewed.resolution());
        assertEquals(reviewed, reportRepository.findById(created.id()).map(
                com.utp.quesale.moderation.application.assemblers.ModerationDtoAssembler::toDto).orElseThrow());
    }

    @Test
    @DisplayName("Descarta un reporte abierto")
    void dismissesReport() {
        ReportDto created = createReport();

        ReportDto dismissed = reportHandler.handle(new DismissReportCommand(created.id(), 99L, "No corresponde"));

        assertEquals(ReportStatus.DISMISSED, dismissed.status());
        assertEquals(99L, dismissed.reviewedByAccountId());
    }

    @Test
    @DisplayName("Lanza ReportNotFoundException al resolver un reporte inexistente")
    void failsToResolveMissingReport() {
        assertThrows(ReportNotFoundException.class,
                () -> reportHandler.handle(new ReviewReportCommand(404L, 99L, "Resuelto")));
    }

    @Test
    @DisplayName("Crea un bloqueo y devuelve su DTO persistido")
    void createsBlock() {
        BlockDto dto = blockHandler.handle(new CreateBlockCommand(12L, 34L));

        assertEquals(1L, dto.id());
        assertEquals(12L, dto.blockerId());
        assertEquals(34L, dto.blockedId());
    }

    @Test
    @DisplayName("No duplica un bloqueo existente")
    void rejectsDuplicateBlock() {
        blockHandler.handle(new CreateBlockCommand(12L, 34L));

        assertThrows(IllegalStateException.class, () -> blockHandler.handle(new CreateBlockCommand(12L, 34L)));
        assertEquals(1, blockRepository.findAllByBlockerId(12L).size());
    }

    @Test
    @DisplayName("Elimina un bloqueo existente")
    void deletesBlock() {
        blockHandler.handle(new CreateBlockCommand(12L, 34L));

        assertDoesNotThrow(() -> blockHandler.handle(new DeleteBlockCommand(12L, 34L)));
        assertTrue(blockRepository.findAllByBlockerId(12L).isEmpty());
    }

    @Test
    @DisplayName("Lanza BlockNotFoundException al eliminar un bloqueo inexistente")
    void failsToDeleteMissingBlock() {
        assertThrows(BlockNotFoundException.class,
                () -> blockHandler.handle(new DeleteBlockCommand(12L, 34L)));
    }

    private ReportDto createReport() {
        return reportHandler.handle(new CreateReportCommand(
                12L, ReportTargetType.STUDENT, 34L, ReportReason.HARASSMENT, "Mensajes insistentes"));
    }
}
