package com.utp.quesale.profiles.application.exceptions;

import com.utp.quesale.shared.domain.exceptions.ResourceNotFoundException;

public class PhotoNotFoundException extends ResourceNotFoundException {
    public PhotoNotFoundException() {
        super("Photo not found");
    }
}
