package com.utp.quesale.shared.infrastructure.events;

import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Prueba el despachador simulando la transacción: no necesita Spring ni base de datos
class SpringDomainEventDispatcherTest {

    private static class SampleAggregate extends AbstractDomainAggregateRoot<SampleAggregate> {
        void happen(Object event) {
            registerDomainEvent(event);
        }
    }

    private final List<Object> published = new ArrayList<>();
    private final ApplicationEventPublisher publisher = published::add;
    private final SpringDomainEventDispatcher dispatcher = new SpringDomainEventDispatcher(publisher);

    @BeforeEach
    void startTransaction() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void endTransaction() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    private void commit() {
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
    }

    @Test
    @DisplayName("Publica los eventos solo cuando la transacción se confirma")
    void publishesOnlyAfterCommit() {
        SampleAggregate aggregate = new SampleAggregate();
        aggregate.happen("creado");

        dispatcher.dispatch(aggregate);
        assertTrue(published.isEmpty());

        commit();
        assertEquals(List.of("creado"), published);
    }

    @Test
    @DisplayName("Si la transacción se revierte, no se publica nada")
    void publishesNothingWithoutCommit() {
        SampleAggregate aggregate = new SampleAggregate();
        aggregate.happen("creado");

        dispatcher.dispatch(aggregate);

        assertTrue(published.isEmpty());
    }

    @Test
    @DisplayName("Un evento se publica una sola vez")
    void doesNotPublishTwice() {
        SampleAggregate aggregate = new SampleAggregate();
        aggregate.happen("creado");
        dispatcher.dispatch(aggregate);

        commit();
        commit();

        assertEquals(1, published.size());
    }

    @Test
    @DisplayName("Sin transacción activa no se puede despachar")
    void requiresActiveTransaction() {
        endTransaction();
        SampleAggregate aggregate = new SampleAggregate();
        aggregate.happen("creado");

        assertThrows(IllegalStateException.class, () -> dispatcher.dispatch(aggregate));
    }

    @Test
    @DisplayName("No se despacha un agregado nulo")
    void rejectsNullAggregate() {
        assertThrows(NullPointerException.class, () -> dispatcher.dispatch(null));
    }
}
