package io.github.palexdev.materialfx.demo.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UltraMsgApi {
    private static final String API_URL = "https://api.ultramsg.com/instance108826/messages/chat";
    private static final String API_TOKEN = "awj9dz7ikxwfhuaj";

    public static void sendSMS(String phoneNumber, String message) {
        try {
            // Format phone number to international format if not already
            if (!phoneNumber.startsWith("+")) {
                phoneNumber = "+216" + phoneNumber; // Assuming Tunisian numbers
            }

            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Request body in JSON format
            String jsonPayload = String.format("{\"token\":\"%s\",\"to\":\"%s\",\"body\":\"%s\"}", 
                API_TOKEN, phoneNumber, message);

            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("API Response: " + response.toString());
            }

            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Message sent successfully!");
            } else {
                System.out.println("Failed to send message. Response code: " + responseCode);
                // Read error response
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                    System.out.println("Error Response: " + errorResponse.toString());
                }
            }

        } catch (Exception e) {
            System.err.println("Error sending WhatsApp message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
