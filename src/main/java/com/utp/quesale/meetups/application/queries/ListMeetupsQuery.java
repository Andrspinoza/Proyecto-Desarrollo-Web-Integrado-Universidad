package com.utp.quesale.meetups.application.queries;

import com.utp.quesale.meetups.domain.valueobjects.MeetupStatus;
import com.utp.quesale.meetups.domain.valueobjects.MeetupType;

import java.time.LocalDateTime;

public record ListMeetupsQuery(
        Long studentId,
        MeetupType type,
        MeetupStatus status,
        LocalDateTime from,
        LocalDateTime to) {
}
