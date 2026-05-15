package org.example.api.video;

import org.example.api.ApiConfig;
import org.example.api.ApiException;
import org.example.api.ApiHttpClient;
import org.example.api.JsonUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class VideoConferenceApiService {

    private final ApiHttpClient httpClient;
    private final String accountId;
    private final String clientId;
    private final String clientSecret;

    public VideoConferenceApiService() {
        this(new ApiHttpClient(),
                ApiConfig.requiredEnv("ZOOM_ACCOUNT_ID"),
                ApiConfig.requiredEnv("ZOOM_CLIENT_ID"),
                ApiConfig.requiredEnv("ZOOM_CLIENT_SECRET"));
    }

    public VideoConferenceApiService(ApiHttpClient httpClient, String accountId, String clientId, String clientSecret) {
        this.httpClient = httpClient;
        this.accountId = accountId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public MeetingInfo createMeeting(MeetingRequest request) {
        String token = requestAccessToken();
        String body = """
                {
                  "topic": %s,
                  "type": 2,
                  "start_time": %s,
                  "duration": %d,
                  "timezone": %s,
                  "settings": {
                    "join_before_host": true,
                    "waiting_room": false
                  }
                }
                """.formatted(
                JsonUtils.quote(request.topic()),
                JsonUtils.quote(request.startTimeIso()),
                request.durationMinutes(),
                JsonUtils.quote(request.timezone())
        );

        String response;
        try {
            response = httpClient.postJson(
                    "https://api.zoom.us/v2/users/me/meetings",
                    body,
                    "Authorization", "Bearer " + token
            );
        } catch (ApiException e) {
            throw zoomException(e);
        }

        return new MeetingInfo(
                JsonUtils.extractValueAsString(response, "id"),
                JsonUtils.extractString(response, "join_url"),
                JsonUtils.extractString(response, "start_url"),
                JsonUtils.extractString(response, "password")
        );
    }

    private String requestAccessToken() {
        String credentials = clientId + ":" + clientSecret;
        String basic = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        String body = "grant_type=account_credentials&account_id="
                + URLEncoder.encode(accountId, StandardCharsets.UTF_8);

        String response;
        try {
            response = httpClient.postForm(
                    "https://zoom.us/oauth/token",
                    body,
                    "Authorization", "Basic " + basic
            );
        } catch (ApiException e) {
            throw zoomException(e);
        }

        return JsonUtils.extractString(response, "access_token");
    }

    private static ApiException zoomException(ApiException e) {
        String message = e.getMessage() == null ? "" : e.getMessage();
        if (message.contains("invalid_client")
                || message.contains("app disabled")
                || message.contains("unsupported grant type")
                || message.contains("Unauthorized")) {
            return new ApiException("L'application Zoom n'est pas activee ou les identifiants sont incorrects.", e);
        }
        return e;
    }
}
