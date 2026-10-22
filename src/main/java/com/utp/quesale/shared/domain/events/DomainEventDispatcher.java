package com.utp.quesale.shared.domain.events;

import com.utp.quesale.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

public interface DomainEventDispatcher {
    void dispatch(AbstractDomainAggregateRoot<?> aggregate);
}
