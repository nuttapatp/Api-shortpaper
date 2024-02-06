package org.example;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.example.model.Location;
import org.example.service.FirestoreService;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.example.utils.UtilityMethods.convertPM25ToAQI;


@SpringBootApplication // This annotation is crucial for a Spring Boot application
public class Main {


    public static void main(String[] args) throws IOException {
        System.out.println("GOOGLE_APPLICATION_CREDENTIALS from System: " + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));


        initializeFirebase();

        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        System.out.println("Credentials Path: " + credentialsPath);
        SpringApplication.run(Main.class, args); // Start the Spring Boot application





    }

    public static void fetchAndNotifyUsers(Location location) {


        AirQualityApi api = new AirQualityApi();
        LineNotifier notifier = new LineNotifier("vDVGmuffx5ZPx/NJp9t6QiJj12hlKFis1Mkbv9x5nrL9psjKV6CxrJ839oxyYME4pT919Em6hKaJyIgTXJSaK2guf5zS/KpHLwHnUNL9DzR6DJCQtT/mGkADqrgfheHik7NI+k2DcOKiwZBqlIts6QdB04t89/1O/w1cDnyilFU="); // Replace with your actual token


        try {
            // Fetch data from Google Cloud API

            // Fetch all user IDs from Firestore
            List<String> userIds = FirestoreService.fetchAllUserIds();
            System.out.println("Total user IDs fetched: " + userIds.size());



            // Send notification to each user
            for (String userId : userIds) {
                Location userLocation = FirestoreService.getUserLatestLocation(userId);
                System.out.println("Fetching for user " + userId + " with location: " + userLocation);

                if (userLocation != null) {
                    System.out.println("User " + userId + " Location: Latitude = " + userLocation.getLatitude() + ", Longitude = " + userLocation.getLongitude());

                    // Fetch air quality data using the user's location
                    String response = api.fetchData(userLocation.getLatitude(), userLocation.getLongitude());
//                    System.out.println("API Response: " + response);
                    double pm25Value = api.extractPM25Value(response);
                    int aqi = convertPM25ToAQI(pm25Value); // Converts PM2.5 concentration to AQI


                    // Send a notification to the user with the PM2.5 value
                    String message = "Your current AQI is: " + aqi;

                    System.out.println("Sending to user " + userId + ": " + aqi);


                    notifier.sendLineMessageToUser(message, userId);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                System.out.println("Initializing Firebase...");

                String jsonKeyFilePath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
                GoogleCredentials credentials;

                if (jsonKeyFilePath != null && !jsonKeyFilePath.isEmpty()) {
                    System.out.println("Firebase Credentials Path: " + jsonKeyFilePath);
                    FileInputStream serviceAccount = new FileInputStream(jsonKeyFilePath);
                    credentials = GoogleCredentials.fromStream(serviceAccount);
                } else {
                    String firebaseCredentials = System.getenv("FIREBASE_CREDENTIALS");
                    if (firebaseCredentials == null || firebaseCredentials.isEmpty()) {
                        throw new FileNotFoundException("Firebase credentials are not properly configured.");
                    }
                    InputStream serviceAccount = new ByteArrayInputStream(firebaseCredentials.getBytes(StandardCharsets.UTF_8));
                    credentials = GoogleCredentials.fromStream(serviceAccount);
                }

                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(credentials)
                        .setDatabaseUrl("https://line-storage-4b555-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Initialized Successfully.");
            } else {
                System.out.println("Firebase already initialized.");
            }
        } catch (Exception e) {
            System.out.println("Error initializing Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }








}
