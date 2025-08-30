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
        String prompt = String.format("""
                Tu es un coach sportif intelligent spécialisé dans l'analyse de données d'entraînement multi-sport. Analyse les données JSON d'activités sportives (RUNNING, SWIMMING, CYCLING) et fournis des conseils personnalisés dans le style d'Athlete Intelligence.
                
                **Instructions :**
                1. Adapte l'analyse selon le sport détecté dans le champ "sport"
                2. Compare les performances réelles aux objectifs planifiés pour chaque bloc
                3. Évalue la conformité aux zones de FC : parfait (±2 bpm), bon (±5 bpm), à ajuster (>5 bpm)
                4. Fournis d'abord un aperçu rapide, puis propose une analyse détaillée
                
                **Format de réponse :**
                
                **[SPORT] - [TYPE] | Note: [GRADE]/10**
                
                **Aperçu rapide :**
                Séance [qualificatif] en [durée formatée] sur [distance][unité]. [Métrique selon sport]. FC moyenne [avgHeartRate] bpm. [Bref commentaire sur la conformité au plan]
                
                **Conseil express :**
                [Une phrase motivante de recommandation principale adaptée au sport]                   
                
                **Analyse par bloc :**
                • Bloc 1 - [intensityZone] ([durée]): FC [actualBPMMean] vs cible [plannedBPMMin-plannedBPMMax] → [évaluation détaillée]
                • Bloc 2 - [intensityZone] ([durée]): FC [actualBPMMean] vs cible [plannedBPMMin-plannedBPMMax] → [évaluation détaillée]
                [continuer pour tous les blocs]
                
                **Analyse technique :**
                [Points forts, faiblesses, progression observée - adaptés au sport]
                
                **Spécificités par sport :**
                - **RUNNING** : Focus sur allure, foulée, gestion du dénivelé
                - **CYCLING** : Focus sur vitesse, puissance, cadence, aérodynamisme \s
                - **SWIMMING** : Focus sur technique, respiration, virages, allure au 100m
                
                **Unités à utiliser :**
                - **RUNNING/CYCLING** : distance en km
                - **SWIMMING** : distance en m (si <2000m) ou km (si >2000m)
                
                Adopte un ton professionnel, motivant et factuel. L'aperçu doit être concis et impactant, l'analyse détaillée approfondie et spécialisée selon le sport.
            
            %s
            """, activityJson);

        return String.format("""
            {
              "model": "%s",
              "messages": [
                {
                  "role": "system",
                  "content": "Tu es un coach sportif expert en français. Analyse les données sportives et donne des conseils constructifs et motivants."
                },
                {
                  "role": "user", 
                  "content": %s
                }
              ],
              "max_tokens": %d,
              "temperature": %.1f
            }
            """,
                model,
                objectMapper.writeValueAsString(prompt),
                maxTokens,
                temperature
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
            String content = choices.get(0).get("message").get("content").asText();

            JsonNode usage = jsonResponse.get("usage");
            if (usage != null && usage.has("total_tokens")) {
                int totalTokens = usage.get("total_tokens").asInt();
                content += String.format("\n\nTokens utilisés: %d", totalTokens);
            }

            return content;
        }

        return "Aucune réponse dans la réponse JSON";
    }

    public String pingGroq() {
        if (apiKey == null || apiKey.isEmpty() || "your-groq-key-here".equals(apiKey)) {
            return "Clé API Groq non configurée";
        }

        try {
            String testJson = String.format("""
                {
                  "model": "%s",
                  "messages": [
                    {"role": "user", "content": "Dis juste 'PING OK' uniquement"}
                  ],
                  "max_tokens": 10
                }
                """, model);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(testJson))
                    .timeout(Duration.ofSeconds(timeout))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.body());
                String content = jsonResponse.get("choices").get(0)
                        .get("message").get("content").asText();
                return content;
            } else {
                return "Erreur ping (" + response.statusCode() + ")";
            }

        } catch (Exception e) {
            return "Problème ping : " + e.getMessage();
        }
    }
}