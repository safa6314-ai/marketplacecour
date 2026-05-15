package org.example.api;

public final class JsonUtils {

    private JsonUtils() {
    }

    public static String quote(String value) {
        if (value == null) {
            return "null";
        }

        StringBuilder escaped = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (ch < 32) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                }
            }
        }
        escaped.append('"');
        return escaped.toString();
    }

    public static String extractString(String json, String fieldName) {
        String marker = quote(fieldName) + ":";
        int markerIndex = json.indexOf(marker);
        if (markerIndex < 0) {
            return "";
        }

        int valueStart = json.indexOf('"', markerIndex + marker.length());
        if (valueStart < 0) {
            return "";
        }

        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int i = valueStart + 1; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (escaped) {
                switch (ch) {
                    case '"' -> value.append('"');
                    case '\\' -> value.append('\\');
                    case '/' -> value.append('/');
                    case 'b' -> value.append('\b');
                    case 'f' -> value.append('\f');
                    case 'n' -> value.append('\n');
                    case 'r' -> value.append('\r');
                    case 't' -> value.append('\t');
                    case 'u' -> {
                        if (i + 4 < json.length()) {
                            String hex = json.substring(i + 1, i + 5);
                            try {
                                value.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                value.append("\\u").append(hex);
                                i += 4;
                            }
                        } else {
                            value.append("\\u");
                        }
                    }
                    default -> value.append(ch);
                }
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == '"') {
                return value.toString();
            } else {
                value.append(ch);
            }
        }
        return "";
    }

    public static String extractValueAsString(String json, String fieldName) {
        String marker = quote(fieldName) + ":";
        int markerIndex = json.indexOf(marker);
        if (markerIndex < 0) {
            return "";
        }

        int valueStart = markerIndex + marker.length();
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart < json.length() && json.charAt(valueStart) == '"') {
            return extractString(json, fieldName);
        }

        int valueEnd = valueStart;
        while (valueEnd < json.length()) {
            char ch = json.charAt(valueEnd);
            if (ch == ',' || ch == '}' || Character.isWhitespace(ch)) {
                break;
            }
            valueEnd++;
        }

        return json.substring(valueStart, valueEnd);
    }
}
