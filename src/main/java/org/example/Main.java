package org.example;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.example.model.Location;
import org.example.service.FirestoreService;
import com.google.auth.oauth2.GoogleCredentials;
import org.example.utils.UtilityMethods;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.*;
import java.util.List;

import static org.example.utils.UtilityMethods.convertPM25ToAQI;
@SpringBootApplication // This annotation is crucial for a Spring Boot application
@ComponentScan(basePackages = {"org.example"})
public class Main {


    public static void main(String[] args) throws IOException {
        String firebaseCredentials = System.getenv("FIREBASE_CREDENTIALS");
        if (firebaseCredentials == null || firebaseCredentials.isEmpty()) {
            System.out.println("Firebase credentials are not set or are empty.");
        } else {
            System.out.println("Firebase credentials are configured.");
        }
        initializeFirebase();
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

    public static void sendNotificationToUser(String userId, String message) {

        System.out.println("hello");
        try {
            System.out.println("hello2");

            FirestoreService firestoreService = new FirestoreService();
            Location userLocation = firestoreService.getUserLatestLocation(userId); // Get user's location

            System.out.println(userLocation);
            System.out.println("hello3");

            if(userLocation != null) {
                AirQualityApi api = new AirQualityApi();
                LineNotifier notifier = new LineNotifier("vDVGmuffx5ZPx/NJp9t6QiJj12hlKFis1Mkbv9x5nrL9psjKV6CxrJ839oxyYME4pT919Em6hKaJyIgTXJSaK2guf5zS/KpHLwHnUNL9DzR6DJCQtT/mGkADqrgfheHik7NI+k2DcOKiwZBqlIts6QdB04t89/1O/w1cDnyilFU=");


                System.out.println("hello4");

                // Fetch air quality data using the user's location
                String response = api.fetchData(userLocation.getLatitude(), userLocation.getLongitude());
                System.out.println("API Response: " + response);


                double pm25Value = api.extractPM25Value(response);
                int aqi = convertPM25ToAQI(pm25Value);
                System.out.println("AQI value: " + aqi);

                // Send a notification to the user with the PM2.5 value
                String notificationMessage = "Your current AQI is: " + aqi;
                notifier.sendLineMessageToUser(notificationMessage, userId);
                System.out.println("Notification sent to user " + userId + ": " + notificationMessage);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            GoogleCredentials credentials;
            String firebaseCredentials = System.getenv("FIREBASE_CREDENTIALS");

            if (firebaseCredentials != null && !firebaseCredentials.isEmpty()) {
                // When running on Heroku, use the JSON string directly
                ByteArrayInputStream serviceAccount = new ByteArrayInputStream(firebaseCredentials.getBytes());
                credentials = GoogleCredentials.fromStream(serviceAccount);
            } else {
                // For local development, use the file path
                String jsonKeyFilePath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
                if (jsonKeyFilePath != null && !jsonKeyFilePath.isEmpty()) {
                    FileInputStream serviceAccount = new FileInputStream(jsonKeyFilePath);
                    credentials = GoogleCredentials.fromStream(serviceAccount);
                } else {
                    throw new FileNotFoundException("Firebase credentials are not properly configured.");
                }
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp.initializeApp(options);
        }
    }







}
