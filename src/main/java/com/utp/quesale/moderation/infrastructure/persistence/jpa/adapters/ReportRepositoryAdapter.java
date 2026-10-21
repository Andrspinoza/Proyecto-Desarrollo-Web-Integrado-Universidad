package com.utp.quesale.moderation.infrastructure.persistence.jpa.adapters;

import com.utp.quesale.moderation.domain.aggregates.Report;
import com.utp.quesale.moderation.domain.exceptions.ReportNotFoundException;
import com.utp.quesale.moderation.domain.repositories.ReportRepository;
import com.utp.quesale.moderation.domain.valueobjects.ReportStatus;
import com.utp.quesale.moderation.infrastructure.persistence.jpa.entities.ReportJpaEntity;
import com.utp.quesale.moderation.infrastructure.persistence.jpa.mappers.ReportPersistenceMapper;
import com.utp.quesale.moderation.infrastructure.persistence.jpa.repositories.ReportJpaRepository;
import com.utp.quesale.profiles.infrastructure.persistence.jpa.entities.StudentJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class ReportRepositoryAdapter implements ReportRepository {
    private final ReportJpaRepository repository;
    private final ReportPersistenceMapper mapper;
    private final EntityManager entityManager;

    public ReportRepositoryAdapter(ReportJpaRepository repository,
                                   ReportPersistenceMapper mapper,
                                   EntityManager entityManager) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public Report save(Report report) {
        StudentJpaEntity reporter = entityManager.getReference(StudentJpaEntity.class, report.getReporterId());
        ReportJpaEntity entity = repository.save(mapper.toEntity(report, reporter));
        return mapper.toDomain(entity);
    }

    @Override
    public Report update(Report report) {
        if (report.getId() == null) throw new ReportNotFoundException(null);
        if (!repository.existsById(report.getId())) throw new ReportNotFoundException(report.getId());
        return save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Report> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Report> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Report> findAllByStatus(ReportStatus status) {
        return repository.findAllByStatus(status).stream().map(mapper::toDomain).toList();
    }
}
