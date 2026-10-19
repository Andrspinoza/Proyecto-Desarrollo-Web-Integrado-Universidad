package com.utp.quesale.shared.domain.events;

import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

// Puerto del dominio: los handlers de aplicación lo usan sin saber cómo se publican los eventos
public interface DomainEventDispatcher {
    void dispatch(AbstractDomainAggregateRoot<?> aggregate);
}
