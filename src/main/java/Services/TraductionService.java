package Services;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TraductionService {

    public String traduire(String texte, String source, String cible) {
        try {
            String sourceLang = source.equals("auto") ? "fr" : source;
            String encodedText = URLEncoder.encode(texte, StandardCharsets.UTF_8);
            String urlStr = "https://api.mymemory.translated.net/get?q="
                    + encodedText + "&langpair=" + sourceLang + "|" + cible;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() != 200) return "[Erreur traduction]";

            String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            int start = response.indexOf("\"translatedText\":\"") + 18;
            int end   = response.indexOf("\"", start);
            if (start > 17 && end > start) {
                String result = response.substring(start, end);
                return decodeUnicode(result); //
            }
            return "[Erreur parsing]";

        } catch (Exception e) {
            return "[Erreur: " + e.getMessage() + "]";
        }
    }


    private String decodeUnicode(String input) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            if (i + 5 <= input.length()
                    && input.charAt(i) == '\\'
                    && input.charAt(i + 1) == 'u') {
                try {
                    String hex = input.substring(i + 2, i + 6);
                    sb.append((char) Integer.parseInt(hex, 16));
                    i += 6;
                } catch (NumberFormatException e) {
                    sb.append(input.charAt(i));
                    i++;
                }
            } else {
                sb.append(input.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }
}

