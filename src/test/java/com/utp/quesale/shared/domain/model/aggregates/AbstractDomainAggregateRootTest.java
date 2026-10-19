package com.utp.quesale.shared.domain.model.aggregates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de dominio puras: sin Spring ni base de datos
class AbstractDomainAggregateRootTest {

    private static class SampleAggregate extends AbstractDomainAggregateRoot<SampleAggregate> {
        void happen(Object event) {
            registerDomainEvent(event);
        }
    }

    @Test
    @DisplayName("Entrega los eventos registrados en el orden en que ocurrieron")
    void pullsEventsInOrder() {
        SampleAggregate aggregate = new SampleAggregate();
        aggregate.happen("primero");
        aggregate.happen("segundo");

        assertEquals(List.of("primero", "segundo"), aggregate.pullDomainEvents());
    }

    @Test
    @DisplayName("Al entregar los eventos, la lista queda vacía y no se publican dos veces")
    void clearsEventsAfterPulling() {
        SampleAggregate aggregate = new SampleAggregate();
        aggregate.happen("evento");

        aggregate.pullDomainEvents();

        assertTrue(aggregate.pullDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("Un agregado sin eventos entrega una lista vacía")
    void emptyWhenNothingHappened() {
        assertTrue(new SampleAggregate().pullDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("La lista entregada no se puede modificar")
    void pulledListIsImmutable() {
        SampleAggregate aggregate = new SampleAggregate();
        aggregate.happen("evento");

        assertThrows(UnsupportedOperationException.class, () -> aggregate.pullDomainEvents().add("otro"));
    }

    @Test
    @DisplayName("No se registra un evento nulo")
    void rejectsNullEvent() {
        assertThrows(NullPointerException.class, () -> new SampleAggregate().happen(null));
    }
}
