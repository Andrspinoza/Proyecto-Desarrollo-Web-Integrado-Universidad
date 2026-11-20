package com.utp.quesale.calls.application.dtos;

import java.util.List;

public record IceServerDto(List<String> urls, String username, String credential) {
}
