package com.utp.quesale.moderation.application.handlers.queries;

import com.utp.quesale.moderation.application.assemblers.ModerationDtoAssembler;
import com.utp.quesale.moderation.application.dtos.ReportDto;
import com.utp.quesale.moderation.application.queries.ListReportsQuery;
import com.utp.quesale.moderation.domain.repositories.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListReportsQueryHandler {

    private final ReportRepository reportRepository;

    public ListReportsQueryHandler(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<ReportDto> handle(ListReportsQuery query) {
        if (query.status() == null) {
            return reportRepository.findAll().stream()
                    .map(ModerationDtoAssembler::toDto)
                    .toList();
        }
        return reportRepository.findAllByStatus(query.status()).stream()
                .map(ModerationDtoAssembler::toDto)
                .toList();
    }
}
