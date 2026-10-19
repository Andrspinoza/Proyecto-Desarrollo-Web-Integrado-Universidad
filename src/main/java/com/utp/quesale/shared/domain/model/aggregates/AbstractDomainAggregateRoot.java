package com.utp.quesale.shared.domain.model.aggregates;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Raíz de agregado de dominio puro: sin JPA, Spring Data ni APIs de eventos de Spring
public abstract class AbstractDomainAggregateRoot<T extends AbstractDomainAggregateRoot<T>> {

    private final List<Object> pendingEvents = new ArrayList<>();

    protected final void registerDomainEvent(Object event) {
        pendingEvents.add(Objects.requireNonNull(event, "event"));
    }

    // La capa de aplicación entrega estos eventos al despachador después de guardar
    public final List<Object> pullDomainEvents() {
        List<Object> events = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return events;
    }
}
