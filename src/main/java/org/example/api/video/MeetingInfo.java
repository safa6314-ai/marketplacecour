package org.example.api.video;

public record MeetingInfo(
        String id,
        String joinUrl,
        String startUrl,
        String password
) {
}
