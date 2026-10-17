package com.utp.quesale.meetups.domain.exceptions;

import com.utp.quesale.shared.domain.exceptions.ResourceNotFoundException;

public class MeetupNotFoundException extends ResourceNotFoundException {
    public MeetupNotFoundException(Long meetupId) {
        super("Meetup with ID %s not found".formatted(meetupId));
    }
}