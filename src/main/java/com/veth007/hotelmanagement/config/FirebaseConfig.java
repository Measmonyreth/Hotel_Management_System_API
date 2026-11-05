package com.veth007.hotelmanagement.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

@Configuration
public class FirebaseConfig {

    private static final String APP_NAME = FirebaseApp.DEFAULT_APP_NAME;
    private final Environment environment;

    public FirebaseConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        for (FirebaseApp existing : FirebaseApp.getApps()) {
            if (existing.getName().equals(APP_NAME)) {
                return existing;
            }
        }

        GoogleCredentials credentials = loadCredentials();
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        return FirebaseApp.initializeApp(options);
    }

    private GoogleCredentials loadCredentials() throws IOException {
        String credentialsJson = environment.getProperty("FIREBASE_SERVICE_ACCOUNT_JSON");
        if (credentialsJson != null && !credentialsJson.isBlank()) {
            return GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)));
        }

        String credentialsFile = environment.getProperty("FIREBASE_CREDENTIALS_FILE",
                environment.getProperty("GOOGLE_APPLICATION_CREDENTIALS"));
        if (credentialsFile != null && !credentialsFile.isBlank()) {
            return loadCredentialsFile(Path.of(credentialsFile));
        }

        // Render Secret Files are mounted under /etc/secrets, not copied into
        // the application's classpath or working directory.
        Path renderSecret = Path.of("/etc/secrets/firebase-service-account.json");
        if (Files.isRegularFile(renderSecret)) {
            return loadCredentialsFile(renderSecret);
        }

        try (InputStream serviceAccount = getClass().getClassLoader()
                .getResourceAsStream("firebase-service-account.json")) {
            if (serviceAccount == null) {
                throw new IOException("Firebase credentials not configured. Set FIREBASE_SERVICE_ACCOUNT_JSON "
                        + "or FIREBASE_CREDENTIALS_FILE on Render.");
            }
            return GoogleCredentials.fromStream(serviceAccount);
        }
    }

    private GoogleCredentials loadCredentialsFile(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IOException("Firebase service account file not found: " + path
                    + ". On Render, add it as a Secret File and set FIREBASE_CREDENTIALS_FILE="
                    + "/etc/secrets/firebase-service-account.json.");
        }
        try (InputStream serviceAccount = Files.newInputStream(path)) {
            return GoogleCredentials.fromStream(serviceAccount);
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
