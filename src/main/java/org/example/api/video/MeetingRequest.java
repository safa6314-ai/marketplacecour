package org.example.api.video;

public record MeetingRequest(
        String topic,
        String startTimeIso,
        int durationMinutes,
        String timezone
) {
}
