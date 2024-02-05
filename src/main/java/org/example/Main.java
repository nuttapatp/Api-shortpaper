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
import java.util.List;

import static org.example.utils.UtilityMethods.convertPM25ToAQI;


@SpringBootApplication // This annotation is crucial for a Spring Boot application
public class Main {


    public static void main(String[] args) throws IOException {
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


    public static void initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            GoogleCredentials credentials;
            // When running on App Engine, use the default credentials
            if (System.getenv("GAE_ENV") != null && System.getenv("GAE_ENV").equals("standard")) {
                credentials = GoogleCredentials.getApplicationDefault();
            } else {
                // When running locally, use the path to the service account key
                String pathToCredentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
                if (pathToCredentials == null || pathToCredentials.isEmpty()) {
                    throw new FileNotFoundException("Firebase credentials path is not found in environment variables.");
                }
                FileInputStream serviceAccount = new FileInputStream(pathToCredentials);
                credentials = GoogleCredentials.fromStream(serviceAccount);
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(credentials)
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }







}
