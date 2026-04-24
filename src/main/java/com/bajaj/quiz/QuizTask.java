package com.bajaj.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class QuizTask {
    // IMPORTANT: When you are ready for final submission, change this to your ACTUAL registration number.
    // For testing, append _TEST so you do not use up your limited submission attempts on the server.
    private static final String DEFAULT_REG_NO = "2024CS101"; 
    private static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    
    public static void main(String[] args) {
        String regNo = DEFAULT_REG_NO;
        if (args.length > 0) {
            regNo = args[0];
        }

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        ObjectMapper mapper = new ObjectMapper();

        // Track unique events using a Set of "roundId_participant" strings
        Set<String> processedEvents = new HashSet<>();
        
        // Map to keep track of total scores per participant
        Map<String, Integer> participantScores = new HashMap<>();

        try {
            System.out.println("Starting polls for regNo: " + regNo);
            for (int poll = 0; poll < 10; poll++) {
                System.out.println("Polling index: " + poll + " ...");
                String url = BASE_URL + "/quiz/messages?regNo=" + regNo + "&poll=" + poll;
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    JsonNode root = mapper.readTree(response.body());
                    JsonNode eventsNode = root.get("events");
                    if (eventsNode != null && eventsNode.isArray()) {
                        for (JsonNode eventNode : eventsNode) {
                            String roundId = eventNode.get("roundId").asText();
                            String participant = eventNode.get("participant").asText();
                            int score = eventNode.get("score").asInt();

                            // Deduplication logic
                            String uniqueEventKey = roundId + "|#|" + participant;
                            if (!processedEvents.contains(uniqueEventKey)) {
                                processedEvents.add(uniqueEventKey);
                                participantScores.put(participant, participantScores.getOrDefault(participant, 0) + score);
                                System.out.println("  Processed event -> Round: " + roundId + ", Participant: " + participant + ", Score: " + score);
                            } else {
                                System.out.println("  Duplicate event ignored -> Round: " + roundId + ", Participant: " + participant);
                            }
                        }
                    }
                } else {
                    System.out.println("  Failed poll " + poll + " with status: " + response.statusCode());
                }

                // Mandatory 5 seconds delay, unless it's the last poll
                if (poll < 9) {
                    Thread.sleep(5000);
                }
            }

            // Create leaderboard
            List<Map.Entry<String, Integer>> leaderboardList = new ArrayList<>(participantScores.entrySet());
            // Sorted descending by totalScore
            leaderboardList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            ArrayNode leaderboardNode = mapper.createArrayNode();
            int totalScoreAcrossAllUsers = 0;

            for (Map.Entry<String, Integer> entry : leaderboardList) {
                ObjectNode participantNode = mapper.createObjectNode();
                participantNode.put("participant", entry.getKey());
                participantNode.put("totalScore", entry.getValue());
                leaderboardNode.add(participantNode);
                totalScoreAcrossAllUsers += entry.getValue();
            }

            System.out.println("\nComputed Total Score across all users: " + totalScoreAcrossAllUsers);

            ObjectNode submitPayload = mapper.createObjectNode();
            submitPayload.put("regNo", regNo);
            submitPayload.set("leaderboard", leaderboardNode);

            String payloadString = mapper.writeValueAsString(submitPayload);
            System.out.println("\nSubmission Payload:");
            System.out.println(payloadString);

            // =========================================================================
            // WARNING: DO NOT SUBMIT MULTIPLE TIMES FOR THE SAME REG_NO
            // The validation server tracks attempt counts. Final submit should only be done ONCE.
            // =========================================================================
            
            boolean isDryRun = false; // CHANGE THIS TO false WHEN YOU ARE READY FOR FINAL EXACT SUBMIT

            if (isDryRun) {
                System.out.println("\n[DRY RUN MODE] - Submission bypassed to avoid exhausting your single attempt.");
                System.out.println("Change `isDryRun = false` and `DEFAULT_REG_NO` to your actual ID before your final run.");
            } else {
                // Submit leaderboard
                HttpRequest submitRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/quiz/submit"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payloadString))
                        .build();

                System.out.println("\nSubmitting to validation server...");
                HttpResponse<String> submitResponse = client.send(submitRequest, HttpResponse.BodyHandlers.ofString());
                
                System.out.println("Submit Response Code: " + submitResponse.statusCode());
                System.out.println("Submit Response Body: " + submitResponse.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
