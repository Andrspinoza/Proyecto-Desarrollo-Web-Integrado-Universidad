package com.utp.quesale.connections.application.queries;

import com.utp.quesale.connections.domain.valueobjects.ConnectionStatus;

public record ListConnectionsQuery(Long studentId, ConnectionStatus status) {
}
