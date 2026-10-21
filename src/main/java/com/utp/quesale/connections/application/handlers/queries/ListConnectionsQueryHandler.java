package com.utp.quesale.connections.application.handlers.queries;

import com.utp.quesale.connections.application.assemblers.ConnectionsDtoAssembler;
import com.utp.quesale.connections.application.dtos.ConnectionDto;
import com.utp.quesale.connections.application.queries.ListConnectionsQuery;
import com.utp.quesale.connections.domain.repositories.ConnectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListConnectionsQueryHandler {

    private final ConnectionRepository connectionRepository;

    public ListConnectionsQueryHandler(ConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }

    public List<ConnectionDto> handle(ListConnectionsQuery query) {
        if (query.status() == null) {
            return connectionRepository.findAllByStudentId(query.studentId()).stream()
                    .map(ConnectionsDtoAssembler::toDto)
                    .toList();
        }
        return connectionRepository.findAllByStudentIdAndStatus(query.studentId(), query.status()).stream()
                .map(ConnectionsDtoAssembler::toDto)
                .toList();
    }

    public List<ConnectionDto> handleReceived(Long studentId) {
        return connectionRepository.findAllReceivedByStudentId(studentId).stream()
                .map(ConnectionsDtoAssembler::toDto)
                .toList();
    }

    public List<ConnectionDto> handleSent(Long studentId) {
        return connectionRepository.findAllSentByStudentId(studentId).stream()
                .map(ConnectionsDtoAssembler::toDto)
                .toList();
    }
}
