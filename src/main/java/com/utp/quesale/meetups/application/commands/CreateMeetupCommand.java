package com.utp.quesale.meetups.application.commands;

import com.utp.quesale.meetups.domain.valueobjects.MeetupType;
import com.utp.quesale.meetups.domain.valueobjects.MeetupVisibility;

import java.time.LocalDateTime;

public record CreateMeetupCommand(
        Long organizerId,
        String title,
        String description,
        MeetupType type,
        MeetupVisibility visibility,
        LocalDateTime scheduledAt,
        String placeName,
        String placeAddress,
        String placeDistrict,
        int capacity) {
}
