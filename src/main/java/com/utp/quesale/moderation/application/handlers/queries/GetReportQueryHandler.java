package com.utp.quesale.moderation.application.handlers.queries;

import com.utp.quesale.moderation.application.assemblers.ModerationDtoAssembler;
import com.utp.quesale.moderation.application.dtos.ReportDto;
import com.utp.quesale.moderation.application.queries.GetReportQuery;
import com.utp.quesale.moderation.domain.exceptions.ReportNotFoundException;
import com.utp.quesale.moderation.domain.repositories.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetReportQueryHandler {

    private final ReportRepository reportRepository;

    public GetReportQueryHandler(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public ReportDto handle(GetReportQuery query) {
        return reportRepository.findById(query.reportId())
                .map(ModerationDtoAssembler::toDto)
                .orElseThrow(() -> new ReportNotFoundException(query.reportId()));
    }
}
