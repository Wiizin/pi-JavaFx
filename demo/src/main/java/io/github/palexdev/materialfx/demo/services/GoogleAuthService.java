package io.github.palexdev.materialfx.demo.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.json.JsonObjectParser;
import io.github.palexdev.materialfx.controls.MFXNotificationCenter;
import io.github.palexdev.materialfx.enums.NotificationPos;
import io.github.palexdev.materialfx.notifications.MFXNotificationSystem;
import javafx.util.Duration;

import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GoogleAuthService {
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";
    private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob"; // Special redirect URI for installed applications
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email"
    );

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final GoogleAuthorizationCodeFlow flow;
    private final HttpTransport httpTransport;


    public GoogleAuthService() throws IOException, GeneralSecurityException {
        // Load client secrets from resources
        InputStream in = getClass().getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            System.err.println("File not found at: " + CREDENTIALS_FILE_PATH);
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        } else {
            System.out.println("File loaded successfully: " + CREDENTIALS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(in)
        );

        // Initialize HTTP transport
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Create a data store directory in user home
        File dataStoreDir = new File(System.getProperty("user.home"), ".store/google_auth");
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(dataStoreDir);

        // Create the flow builder with persistent credential storage
        flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                SCOPES
        )
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .setApprovalPrompt("force") // Force showing the consent screen
                .build();
    }



    public static class UserInfo {
        private final String email;
        private final String name;
        private final String firstName;
        private final String lastName;
        private final String pictureUrl;

        public UserInfo(String email, String name, String firstName, String lastName, String pictureUrl) {
            this.email = email;
            this.name = name;
            this.firstName = firstName;
            this.lastName = lastName;
            this.pictureUrl = pictureUrl;
        }

        // Add getters
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getPictureUrl() { return pictureUrl; }
    }

    public CompletableFuture<UserInfo> startOAuthFlowAsync() {
        CompletableFuture<UserInfo> future = new CompletableFuture<>();

        // Run the OAuth flow on a background thread
        new Thread(() -> {
            try {
                // Build the authorization URL with the OOB redirect URI
                String authorizationUrl = flow.newAuthorizationUrl()
                        .setRedirectUri(REDIRECT_URI)
                        .build();

                System.out.println("Using out-of-band OAuth flow for desktop applications");
                System.out.println("Authorization URL: " + authorizationUrl);

                // Step 1: Show the initial instruction dialog
                Platform.runLater(() -> {
                    Alert instructionAlert = new Alert(AlertType.INFORMATION);
                    instructionAlert.setTitle("Google Authentication");
                    instructionAlert.setHeaderText("Browser will open for Google Sign-In");
                    instructionAlert.setContentText("After signing in and granting permissions, Google will display an authorization code.\n\n" +
                            "Copy this code and paste it in the next dialog that appears.");
                    instructionAlert.showAndWait(); // Wait for the user to acknowledge
                });

                // Step 2: Open the browser for the user to authenticate
                boolean browserOpened = false;
                try {
                    Desktop.getDesktop().browse(new URI(authorizationUrl));
                    browserOpened = true;
                } catch (Exception e) {
                    System.err.println("Failed to open browser automatically: " + e.getMessage());
                }

                // If the browser failed to open, show the URL in a dialog
                if (!browserOpened) {
                    Platform.runLater(() -> {
                        Alert browserFailedAlert = new Alert(AlertType.WARNING);
                        browserFailedAlert.setTitle("Browser Launch Failed");
                        browserFailedAlert.setHeaderText("Could not open browser automatically");
                        browserFailedAlert.setContentText("Please manually open the following URL in your browser:\n\n" + authorizationUrl);
                        browserFailedAlert.showAndWait(); // Wait for the user to acknowledge
                    });
                }

                // Step 3: Wait for the user to enter the authorization code
                CompletableFuture<String> codeFuture = new CompletableFuture<>();

                Platform.runLater(() -> {
                    TextInputDialog codeDialog = new TextInputDialog();
                    codeDialog.setTitle("Enter Authorization Code");
                    codeDialog.setHeaderText("Enter the authorization code from Google");
                    codeDialog.setContentText("After completing the Google authorization, copy the code shown on the page and paste it here:");

                    Optional<String> result = codeDialog.showAndWait();
                    if (result.isPresent() && !result.get().trim().isEmpty()) {
                        codeFuture.complete(result.get().trim()); // Complete the future with the code
                    } else {
                        codeFuture.completeExceptionally(new IOException("No authorization code provided"));
                    }
                });

                // Wait for the user to provide the authorization code
                String code = codeFuture.get(); // Blocking call
                System.out.println("Authorization code received: " + code);

                // Step 4: Complete the OAuth flow with the authorization code
                GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                        .setRedirectUri(REDIRECT_URI)
                        .execute();

                System.out.println("Token response received: " + tokenResponse);

                // Create and store the credential
                Credential credential = flow.createAndStoreCredential(tokenResponse, "user");

                // Step 5: Fetch user info using the access token
                HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);
                GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v3/userinfo");
                HttpRequest request = requestFactory.buildGetRequest(url);
                request.setParser(new JsonObjectParser(JSON_FACTORY));

                System.out.println("Fetching user info from Google API...");

                // Parse the response as a map
                @SuppressWarnings("unchecked")
                Map<String, Object> userInfoMap = request.execute().parseAs(Map.class);

                System.out.println("User info received: " + userInfoMap);

                // Extract user info from the map
                String email = (String) userInfoMap.get("email");
                String name = (String) userInfoMap.get("name");
                String givenName = (String) userInfoMap.get("given_name");
                String familyName = (String) userInfoMap.get("family_name");
                String pictureUrl = (String) userInfoMap.get("picture");

                // Complete the future with the user info
                future.complete(new UserInfo(
                        email,
                        name,
                        givenName,
                        familyName,
                        pictureUrl
                ));
            } catch (Exception e) {
                // Handle errors
                future.completeExceptionally(e);

                System.err.println("Error during OAuth flow: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    Alert errorAlert = new Alert(AlertType.ERROR);
                    errorAlert.setTitle("Authentication Error");
                    errorAlert.setHeaderText("Failed to complete Google authentication");
                    errorAlert.setContentText("Error: " + e.getMessage() + "\n\nPlease try again.");
                    errorAlert.showAndWait();
                });
            }
        }).start();

        return future;
    }
}
