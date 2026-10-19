package com.utp.quesale.meetups.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ResourceNotFoundException;

public class ParticipationNotFoundException extends ResourceNotFoundException {
    public ParticipationNotFoundException(Long meetupId, Long studentId) {
        super("Participation for student %s in meetup %s not found".formatted(studentId, meetupId));
    }
}