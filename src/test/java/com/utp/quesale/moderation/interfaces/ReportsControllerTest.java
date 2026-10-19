package com.utp.quesale.moderation.interfaces;

import com.utp.quesale.moderation.application.commands.CreateReportCommand;
import com.utp.quesale.moderation.application.commands.DismissReportCommand;
import com.utp.quesale.moderation.application.commands.ReviewReportCommand;
import com.utp.quesale.moderation.application.dtos.ReportDto;
import com.utp.quesale.moderation.application.handlers.commands.ReportCommandHandler;
import com.utp.quesale.moderation.application.handlers.queries.GetReportQueryHandler;
import com.utp.quesale.moderation.application.handlers.queries.ListReportsQueryHandler;
import com.utp.quesale.moderation.application.queries.GetReportQuery;
import com.utp.quesale.moderation.application.queries.ListReportsQuery;
import com.utp.quesale.moderation.domain.valueobjects.ReportReason;
import com.utp.quesale.moderation.domain.valueobjects.ReportStatus;
import com.utp.quesale.moderation.domain.valueobjects.ReportTargetType;
import com.utp.quesale.moderation.interfaces.controllers.ReportsController;
import com.utp.quesale.moderation.interfaces.resources.ResolveReportResource;
import com.utp.quesale.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportsControllerTest {

    private final ReportCommandHandler commandHandler = mock(ReportCommandHandler.class);
    private final GetReportQueryHandler getHandler = mock(GetReportQueryHandler.class);
    private final ListReportsQueryHandler listHandler = mock(ListReportsQueryHandler.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ReportsController(commandHandler, getHandler, listHandler))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("POST crea un reporte usando el estudiante del header")
    void createsReport() throws Exception {
        ReportDto dto = reportDto(1L, ReportStatus.OPEN, null, null, null);
        when(commandHandler.handle(any(CreateReportCommand.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/reports")
                        .header("X-Student-Id", 12)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetType":"STUDENT","targetId":34,"reason":"HARASSMENT","detail":"Detalle"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/reports/1"))
                .andExpect(jsonPath("$.reporterId").value(12))
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(commandHandler).handle(new CreateReportCommand(
                12L, ReportTargetType.STUDENT, 34L, ReportReason.HARASSMENT, "Detalle"));
    }

    @Test
    @DisplayName("GET lista reportes aplicando el filtro solicitado")
    void listsReportsByStatus() throws Exception {
        when(listHandler.handle(new ListReportsQuery(ReportStatus.OPEN)))
                .thenReturn(List.of(reportDto(1L, ReportStatus.OPEN, null, null, null)));

        mockMvc.perform(get("/api/v1/reports").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    @Test
    @DisplayName("GET obtiene el reporte por id")
    void getsReportById() throws Exception {
        when(getHandler.handle(new GetReportQuery(1L)))
                .thenReturn(reportDto(1L, ReportStatus.OPEN, null, null, null));

        mockMvc.perform(get("/api/v1/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.targetType").value("STUDENT"));
    }

    @Test
    @DisplayName("PATCH review toma la identidad provisional del header")
    void reviewsReport() throws Exception {
        when(commandHandler.handle(any(ReviewReportCommand.class)))
                .thenReturn(reportDto(1L, ReportStatus.REVIEWED, 99L, Instant.parse("2026-10-19T12:00:00Z"),
                        "Atendido"));

        mockMvc.perform(patch("/api/v1/reports/1/review")
                        .header("X-Student-Id", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"Atendido\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVIEWED"))
                .andExpect(jsonPath("$.reviewedByAccountId").value(99))
                .andExpect(jsonPath("$.resolution").value("Atendido"));

        verify(commandHandler).handle(new ReviewReportCommand(1L, 99L, "Atendido"));
    }

    @Test
    @DisplayName("PATCH dismiss descarta el reporte con la resolución indicada")
    void dismissesReport() throws Exception {
        when(commandHandler.handle(any(DismissReportCommand.class)))
                .thenReturn(reportDto(1L, ReportStatus.DISMISSED, 99L, Instant.parse("2026-10-19T12:00:00Z"),
                        "No corresponde"));

        mockMvc.perform(patch("/api/v1/reports/1/dismiss")
                        .header("X-Student-Id", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"No corresponde\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISMISSED"))
                .andExpect(jsonPath("$.resolution").value("No corresponde"));

        verify(commandHandler).handle(new DismissReportCommand(1L, 99L, "No corresponde"));
    }

    @Test
    @DisplayName("PATCH rechaza una resolución vacía")
    void rejectsBlankResolution() throws Exception {
        mockMvc.perform(patch("/api/v1/reports/1/review")
                        .header("X-Student-Id", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    private static ReportDto reportDto(
            Long id, ReportStatus status, Long reviewerId, Instant reviewedAt, String resolution) {
        return new ReportDto(id, 12L, ReportTargetType.STUDENT, 34L, ReportReason.HARASSMENT,
                "Detalle", status, Instant.parse("2026-10-19T10:00:00Z"), reviewerId, reviewedAt, resolution);
    }
}
