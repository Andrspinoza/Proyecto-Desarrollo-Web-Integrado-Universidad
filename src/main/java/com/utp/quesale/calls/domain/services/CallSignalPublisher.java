package com.utp.quesale.calls.domain.services;

import com.utp.quesale.calls.domain.valueobjects.CallSignal;

public interface CallSignalPublisher {

    void publish(CallSignal signal);
}
