package com.rotopay.expensetracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rotopay.expensetracker.api.common.dto.TransactionClassificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for interacting with Ollama local LLM.
 * Handles API calls to local Ollama instance for transaction classification.
 *
 * Prerequisites:
 * - Ollama installed: https://ollama.ai
 * - Model pulled: ollama pull mistral (or your chosen model)
 * - Running: ollama serve (on default port 11434)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaClientService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${pfa.llm.endpoint:http://localhost:11434}")
    private String ollamaEndpoint;

    @Value("${pfa.llm.model:mistral}")
    private String modelName;

    @Value("${pfa.llm.timeout:30000}")
    private Integer timeout;

    /**
     * Test connection to Ollama instance.
     * Call this on application startup to verify Ollama is running.
     */
    public boolean testConnection() {
        try {
            log.info("Testing connection to Ollama at: {}", ollamaEndpoint);

            String testUrl = ollamaEndpoint + "/api/tags";
            JsonNode response = restTemplate.getForObject(testUrl, JsonNode.class);

            if (response != null && response.has("models")) {
                log.info("✓ Connection successful. Available models: {}", response.get("models"));
                return true;
            }
        } catch (RestClientException e) {
            log.error("✗ Failed to connect to Ollama. Make sure it's running: ollama serve", e);
        }
        return false;
    }

    /**
     * Classify a single transaction using LLM.
     * Returns category, confidence score, and reasoning.
     */
    public TransactionClassificationResult classifyTransaction(String description, String amount) {
        log.debug("Classifying transaction: {} | Amount: {}", description, amount);

        try {
            String prompt = buildClassificationPrompt(description, amount);
            String response = callOllama(prompt);

            return parseClassificationResponse(response);
        } catch (Exception e) {
            log.error("Error classifying transaction: {}", description, e);
            return TransactionClassificationResult.builder()
                    .category("Other")
                    .confidenceScore(0.0f)
                    .reasoning("Classification failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Extract transactions from raw text (from PDF or statement).
     * Returns a list of extracted transaction details.
     */
    public String extractTransactionsFromText(String rawText) {
        log.debug("Extracting transactions from text, length: {}", rawText.length());

        try {
            String prompt = buildExtractionPrompt(rawText);
            return callOllama(prompt);
        } catch (Exception e) {
            log.error("Error extracting transactions from text", e);
            return "[]";
        }
    }

    /**
     * Batch classify multiple transactions.
     * More efficient than individual calls.
     */
    public Map<String, TransactionClassificationResult> batchClassifyTransactions(
            Map<String, String> transactions) {

        log.debug("Batch classifying {} transactions", transactions.size());

        Map<String, TransactionClassificationResult> results = new HashMap<>();

        for (Map.Entry<String, String> entry : transactions.entrySet()) {
            String txnId = entry.getKey();
            String description = entry.getValue();

            TransactionClassificationResult result = classifyTransaction(description, "");
            results.put(txnId, result);
        }

        return results;
    }

    /**
     * Detect if a transaction is recurring (subscription-like).
     */
    public boolean detectRecurring(String merchantName, String description) {
        log.debug("Detecting if transaction is recurring: {} | {}", merchantName, description);

        try {
            String prompt = buildRecurringDetectionPrompt(merchantName, description);
            String response = callOllama(prompt);

            return response.toLowerCase().contains("\"is_recurring\": true") ||
                    response.toLowerCase().contains("\"recurring\": true");
        } catch (Exception e) {
            log.error("Error detecting recurring transaction", e);
            return false;
        }
    }

    /**
     * Analyze spending patterns (called for anomaly detection).
     */
    public String analyzeSpendingPattern(String categoryName, double amount) {
        log.debug("Analyzing spending pattern: {} | Amount: {}", categoryName, amount);

        try {
            String prompt = buildPatternAnalysisPrompt(categoryName, amount);
            return callOllama(prompt);
        } catch (Exception e) {
            log.error("Error analyzing spending pattern", e);
            return "normal";
        }
    }

    /**
     * Parse merchant name from raw transaction description.
     * Useful when merchant field is not clearly separated.
     */
    public String extractMerchantName(String rawDescription) {
        log.debug("Extracting merchant name from: {}", rawDescription);

        try {
            String prompt = buildMerchantExtractionPrompt(rawDescription);
            String response = callOllama(prompt);

            // Parse JSON response
            JsonNode jsonResponse = objectMapper.readTree(response);
            return jsonResponse.has("merchant")
                    ? jsonResponse.get("merchant").asText()
                    : rawDescription.split(" ")[0];
        } catch (Exception e) {
            log.warn("Error extracting merchant name, using raw description", e);
            return rawDescription.split(" ")[0];
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Call Ollama API with a prompt.
     * Streams response for efficiency.
     */
    private String callOllama(String prompt) throws Exception {
        log.debug("Calling Ollama with prompt length: {}", prompt.length());

        String url = ollamaEndpoint + "/api/generate";

        Map<String, Object> request = new HashMap<>();
        request.put("model", modelName);
        request.put("prompt", prompt);
        request.put("stream", false);
        request.put("temperature", 0.3); // Lower temp = more consistent
        request.put("top_p", 0.9);
        request.put("top_k", 40);

        try {
            String requestJson = objectMapper.writeValueAsString(request);
            log.trace("Ollama request: {}", requestJson);

            // Use PostForObject with custom timeout
            JsonNode response = restTemplate.postForObject(url, request, JsonNode.class);

            if (response != null && response.has("response")) {
                String result = response.get("response").asText();
                log.trace("Ollama response: {}", result);
                return result;
            }

            throw new RuntimeException("Invalid Ollama response");
        } catch (RestClientException e) {
            log.error("Ollama API call failed. Is it running? (ollama serve)", e);
            throw e;
        }
    }

    // ==================== PROMPT BUILDERS ====================

    /**
     * Build prompt for transaction classification.
     * Provides category list and expects JSON response.
     */
    private String buildClassificationPrompt(String description, String amount) {
        return """
            You are a financial transaction classifier. Classify this transaction into ONE category.
            
            Transaction Details:
            Description: %s
            Amount: %s
            
            Available Categories:
            - Groceries: Food & grocery shopping
            - Utilities: Electricity, water, internet, phone
            - Entertainment: Movies, games, dining out, hobbies
            - Transportation: Gas, parking, public transit, ride-share
            - Healthcare: Medical, prescriptions, fitness, gym
            - Salary/Income: Wages, bonuses, refunds
            - Subscriptions: Software, streaming, memberships
            - Shopping: Clothing, electronics, general retail
            - Dining & Restaurants: Restaurants, cafes, food delivery
            - Insurance: Health, auto, home, life insurance
            - Education: Tuition, books, courses, training
            - Transfer: Internal transfers, savings
            - Other: Uncategorized expenses
            
            Respond ONLY with valid JSON (no markdown, no extra text):
            {
              "category": "Category Name",
              "confidence": 0.95,
              "reasoning": "Why this category"
            }
            """.formatted(description, amount);
    }

    /**
     * Build prompt for extracting transactions from raw text.
     */
    private String buildExtractionPrompt(String rawText) {
        return """
            Extract ALL financial transactions from this text. Each transaction has: date, amount, description, and merchant.
            
            Text to analyze:
            %s
            
            Respond ONLY with a JSON array (no markdown, no extra text):
            [
              {
                "date": "YYYY-MM-DD",
                "amount": "-50.00",
                "description": "Transaction description",
                "merchant": "Merchant name"
              }
            ]
            """.formatted(rawText);
    }

    /**
     * Build prompt for detecting recurring transactions.
     */
    private String buildRecurringDetectionPrompt(String merchantName, String description) {
        return """
            Is this a recurring transaction (subscription, membership, regular payment)?
            
            Merchant: %s
            Description: %s
            
            Respond ONLY with JSON:
            {
              "is_recurring": true/false,
              "frequency": "monthly/weekly/annual/unknown",
              "confidence": 0.0-1.0
            }
            """.formatted(merchantName, description);
    }

    /**
     * Build prompt for pattern analysis.
     */
    private String buildPatternAnalysisPrompt(String categoryName, double amount) {
        return """
            Analyze if this transaction is normal for its category.
            
            Category: %s
            Amount: %.2f
            
            Consider typical spending ranges for this category and whether this amount seems unusual.
            
            Respond ONLY with JSON:
            {
              "pattern": "normal/high/low",
              "anomaly_score": 0.0-1.0,
              "description": "brief explanation"
            }
            """.formatted(categoryName, amount);
    }

    /**
     * Build prompt for merchant extraction.
     */
    private String buildMerchantExtractionPrompt(String rawDescription) {
        return """
            Extract the merchant name from this transaction description.
            
            Description: %s
            
            Respond ONLY with JSON:
            {
              "merchant": "Merchant Name"
            }
            """.formatted(rawDescription);
    }

    /**
     * Parse classification response JSON.
     */
    private TransactionClassificationResult parseClassificationResponse(String responseText)
            throws IOException {

        // Handle case where response might have markdown code blocks
        String cleanedResponse = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim();

        JsonNode jsonNode = objectMapper.readTree(cleanedResponse);

        return TransactionClassificationResult.builder()
                .category(jsonNode.get("category").asText("Other"))
                .confidenceScore(Float.parseFloat(jsonNode.get("confidence").asText("0.5")))
                .reasoning(jsonNode.get("reasoning").asText(""))
                .build();
    }
}
