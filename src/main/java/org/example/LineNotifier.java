package org.example;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LineNotifier {
    private String accessToken;

    public LineNotifier(String accessToken) {
        this.accessToken = accessToken;
    }

    public void sendNotificationToAllUsers(double aqiValue, List<String> userIds) throws Exception {
        String message = "Current PM2.5 concentration is: " + aqiValue + " µg/m³";

        for (String userId : userIds) {
            sendLineMessageToUser(message, userId);
        }
    }

    private List<String> fetchAllUserIds() {
        List<String> userIds = new ArrayList<>();
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("friends").get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (DocumentSnapshot document : documents) {
                userIds.add(document.getId());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return userIds;
    }

    public void sendLineMessageToUser(String message, String userId) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String jsonPayload = createJsonPayload(message, userId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.line.me/v2/bot/message/push"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + this.accessToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response for user " + userId + ": " + response.body());
    }

    private String createJsonPayload(String message, String userId) {
        return "{\"to\": \"" + userId + "\", \"messages\": [{\"type\": \"text\", \"text\": \"" + message + "\"}]}";
    }


}
