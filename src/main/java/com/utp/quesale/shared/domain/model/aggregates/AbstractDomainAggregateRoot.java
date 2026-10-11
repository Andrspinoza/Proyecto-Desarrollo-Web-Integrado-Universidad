package com.utp.quesale.shared.domain.model.aggregates;

import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.Collection;
import java.util.List;

public abstract class AbstractDomainAggregateRoot
        <T extends AbstractDomainAggregateRoot<T>> extends
        AbstractAggregateRoot<T> {

    protected void registerDomainEvent(Object event) {
        super.registerEvent(event);
    }

    @Override
    protected Collection<Object> domainEvents() {
        return super.domainEvents();
    }

    @Override
    protected void clearDomainEvents() {
        super.clearDomainEvents();
    }

    // Agregado por el equipo: la capa de aplicación publicará estos eventos después de guardar
    public List<Object> pullDomainEvents() {
        List<Object> events = List.copyOf(domainEvents());
        clearDomainEvents();
        return events;
    }
}
