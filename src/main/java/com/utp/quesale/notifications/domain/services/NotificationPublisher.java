package com.utp.quesale.notifications.domain.services;

import com.utp.quesale.notifications.domain.valueobjects.MatchNotification;

public interface NotificationPublisher {

    void publishMatch(Long recipientStudentId, MatchNotification notification);
}
