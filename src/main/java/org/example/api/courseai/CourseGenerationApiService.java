package org.example.api.courseai;

import org.example.api.ApiConfig;
import org.example.api.ApiException;
import org.example.api.ApiHttpClient;
import org.example.api.JsonUtils;
import org.example.entities.Chapitres;
import org.example.entities.Cours;

import java.util.ArrayList;
import java.util.List;

public class CourseGenerationApiService {

    private static final String GROQ_CHAT_COMPLETIONS_URL = "https://api.groq.com/openai/v1/chat/completions";

    private final ApiHttpClient httpClient;
    private final String apiKey;
    private final String model;

    public CourseGenerationApiService() {
        this(new ApiHttpClient(),
                ApiConfig.requiredEnv("GROQ_API_KEY"),
                ApiConfig.optionalEnv("GROQ_COURSE_MODEL", "llama-3.3-70b-versatile"));
    }

    public CourseGenerationApiService(ApiHttpClient httpClient, String apiKey, String model) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.model = model;
    }

    public GeneratedCourse generateCourse(String description) {
        return generateCourse(new CourseGenerationRequest(description));
    }

    public GeneratedCourse generateCourse(CourseGenerationRequest request) {
        String body = """
                {
                  "model": %s,
                  "messages": [
                    {
                      "role": "system",
                      "content": %s
                    },
                    {
                      "role": "user",
                      "content": %s
                    }
                  ],
                  "temperature": 0.2,
                  "response_format": { "type": "json_object" }
                }
                """.formatted(
                JsonUtils.quote(model),
                JsonUtils.quote(systemInstructions()),
                JsonUtils.quote(userPrompt(request))
        );

        String response = httpClient.postJson(
                GROQ_CHAT_COMPLETIONS_URL,
                body,
                "Authorization", "Bearer " + apiKey
        );

        String generatedJson = JsonUtils.extractString(response, "content");
        if (generatedJson.isBlank()) {
            throw new ApiException("La reponse Groq ne contient pas de cours genere : " + response);
        }

        return mapGeneratedCourse(generatedJson);
    }

    private static String systemInstructions() {
        return """
                Tu es un assistant pedagogique pour une marketplace de cours.
                A partir d'une idee ou d'une description, genere un cours complet, clair et vendable.
                Le cours doit rester fidele a la demande de l'utilisateur.
                Les chapitres doivent contenir du vrai contenu pedagogique, pas seulement des titres.
                Reponds uniquement avec un objet JSON valide, sans markdown.
                """;
    }

    private static String userPrompt(CourseGenerationRequest request) {
        return """
                Description du cours souhaite :
                %s

                Langue : %s
                Nombre de chapitres souhaite : %d
                Prix suggere si pertinent : %.2f

                Format JSON obligatoire :
                {
                  "titre": "string",
                  "description": "string",
                  "prix": 0,
                  "categorie": "string",
                  "niveau": "Debutant|Intermediaire|Avance",
                  "chapitres": [
                    {
                      "titre": "string",
                      "contenu": "string"
                    }
                  ]
                }
                """.formatted(
                request.description(),
                request.language(),
                request.chapterCount(),
                request.suggestedPrice()
        );
    }

    private static String courseSchema() {
        return """
                {
                  "type": "object",
                  "additionalProperties": false,
                  "properties": {
                    "titre": { "type": "string" },
                    "description": { "type": "string" },
                    "prix": { "type": "number" },
                    "categorie": { "type": "string" },
                    "niveau": { "type": "string", "enum": ["Debutant", "Intermediaire", "Avance"] },
                    "chapitres": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "additionalProperties": false,
                        "properties": {
                          "titre": { "type": "string" },
                          "contenu": { "type": "string" }
                        },
                        "required": ["titre", "contenu"]
                      }
                    }
                  },
                  "required": ["titre", "description", "prix", "categorie", "niveau", "chapitres"]
                }
                """;
    }

    private static GeneratedCourse mapGeneratedCourse(String json) {
        Cours cours = new Cours(
                JsonUtils.extractString(json, "titre"),
                JsonUtils.extractString(json, "description"),
                parseDouble(JsonUtils.extractValueAsString(json, "prix")),
                JsonUtils.extractString(json, "categorie"),
                JsonUtils.extractString(json, "niveau")
        );

        List<Chapitres> chapitres = new ArrayList<>();
        List<String> chapterObjects = extractObjectArray(json, "chapitres");
        for (int i = 0; i < chapterObjects.size(); i++) {
            String chapterJson = chapterObjects.get(i);
            Chapitres chapitre = new Chapitres(
                    JsonUtils.extractString(chapterJson, "titre"),
                    JsonUtils.extractString(chapterJson, "contenu"),
                    i + 1,
                    0
            );
            chapitres.add(chapitre);
        }

        return new GeneratedCourse(cours, chapitres);
    }

    private static double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static List<String> extractObjectArray(String json, String fieldName) {
        List<String> objects = new ArrayList<>();
        String marker = JsonUtils.quote(fieldName) + ":";
        int markerIndex = json.indexOf(marker);
        if (markerIndex < 0) {
            return objects;
        }

        int arrayStart = json.indexOf('[', markerIndex + marker.length());
        if (arrayStart < 0) {
            return objects;
        }

        int depth = 0;
        int objectStart = -1;
        boolean inString = false;
        boolean escaped = false;

        for (int i = arrayStart + 1; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (ch == '\\') {
                escaped = true;
                continue;
            }
            if (ch == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (ch == '{') {
                if (depth == 0) {
                    objectStart = i;
                }
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0 && objectStart >= 0) {
                    objects.add(json.substring(objectStart, i + 1));
                    objectStart = -1;
                }
            } else if (ch == ']' && depth == 0) {
                return objects;
            }
        }
        return objects;
    }
}
