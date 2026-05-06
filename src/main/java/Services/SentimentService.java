package Services;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class SentimentService {

    private static final String API_URL =
            "https://api-inference.huggingface.co/models/distilbert-base-uncased-finetuned-sst-2-english";
    private static final String HF_TOKEN = ""; // laisser vide, ça marche sans clé

    public String analyser(String texte) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            if (!HF_TOKEN.isEmpty())
                conn.setRequestProperty("Authorization", "Bearer " + HF_TOKEN);
            conn.setDoOutput(true);

            String body = "{\"inputs\": \"" + texte.replace("\"", "\\\"").replace("\n", " ") + "\"}";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            InputStream is = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
            String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            if (response.contains("POSITIVE")) return "😊";
            if (response.contains("NEGATIVE")) return "😠";
            return "😐";

        } catch (Exception e) {
            return "😐";
        }
    }
}
