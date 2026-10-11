package com.utp.quesale.shared.interfaces.rest;

import java.time.Instant;

public record ErrorResource(int status, String error, String message, String path, Instant timestamp) {
}
