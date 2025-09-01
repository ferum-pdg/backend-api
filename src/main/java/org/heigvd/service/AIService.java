package org.heigvd.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@ApplicationScoped
public class AIService {

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "groq.api.key")
    String apiKey;

    @ConfigProperty(name = "groq.api.url")
    String apiUrl;

    @ConfigProperty(name = "groq.model")
    String model;

    @ConfigProperty(name = "groq.max-tokens")
    Integer maxTokens;

    @ConfigProperty(name = "groq.temperature")
    Double temperature;

    @ConfigProperty(name = "groq.timeout")
    Integer timeout;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String analyzeSportActivity(String activityJson) {
        if (apiKey == null || apiKey.isEmpty() || "your-groq-key-here".equals(apiKey)) {
            return "Clé API Groq non configurée.";
        }

        System.out.println("API KEY: " + apiKey);

        try {
            String requestBody = createRequestJson(activityJson);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("User-Agent", "Quarkus-FerumsportAnalysis/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(timeout))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            return processResponse(response);

        } catch (Exception e) {
            return "Erreur lors de l'analyse : " + e.getMessage();
        }
    }

    private String createRequestJson(String activityJson) throws Exception {
        String prompt = """
        Ton et style:
        Encourageant et positif: Commence toujours par féliciter l'effort
        Personnalisé: Adapte-toi au type de sport et à la performance

        Éléments à analyser:
        Performance vs objectifs: Compare la durée, distance, calories avec les moyennes
        Zone cardiaque: Évalue si la fréquence cardiaque correspond au type d'entraînement
        Progression: Note les améliorations par rapport aux séances précédentes
        Équilibre effort/récupération: Conseille selon l'intensité

        Format de réponse:
        Félicitations + observation sur la performance + conseil/objectif pour la suite
        sans titres ni paragraphes et le tout de faire une centaine de mots max

        Exemples selon le contexte:
        Sortie tranquille réussie: "Belle sortie en zone 2 ! Parfait pour développer ton endurance de base. Continue à 65-75%% de ta FCmax pour optimiser ces séances."
        Performance exceptionnelle: "Excellente performance ! Tu as maintenu un rythme soutenu sur toute la distance. Prochaine étape : essaie d'ajouter 5-10%% de distance."
        Séance difficile: "Bravo d'avoir terminé cette séance exigeante ! Ton corps s'adapte. Pense à bien récupérer avant le prochain entraînement intensif."

        Données d'entraînement à analyser(ces données concernent qu'un seul sortie):
           \s
        %s
       \s
       \s""".formatted(activityJson);

        return String.format("""
            {
              "model": "%s",
              "messages": [
                {
                  "role": "system",
                  "content": "Tu es un coach sportif expert en français."
                },
                {
                  "role": "user",\s
                  "content": %s
                }
              ]
            }
           \s""",
                model,
                objectMapper.writeValueAsString(prompt)
        );
    }

    private String processResponse(HttpResponse<String> response) throws Exception {
        int statusCode = response.statusCode();
        String body = response.body();

        if (statusCode != 200) {
            return "Erreur API (" + statusCode + ") : " + body;
        }

        JsonNode jsonResponse = objectMapper.readTree(body);
        JsonNode choices = jsonResponse.get("choices");

        if (choices != null && !choices.isEmpty()) {
            return choices.get(0).get("message").get("content").asText();
        }

        return "Aucune réponse dans la réponse JSON";
    }
}