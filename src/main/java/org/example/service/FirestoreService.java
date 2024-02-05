package org.example.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.example.model.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FirestoreService {

    public static List<String> fetchAllUserIds() throws InterruptedException, ExecutionException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> query = db.collection("friends").get();
        List<String> userIds = new ArrayList<>();
        QuerySnapshot querySnapshot = query.get();

        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            userIds.add(document.getId());
        }
        return userIds;
    }

    public static Location getUserLatestLocation(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot documentSnapshot = db.collection("userLocations").document(userId).get().get();
        if (documentSnapshot.exists()) {
            Double latitude = documentSnapshot.getDouble("latitude");
            Double longitude = documentSnapshot.getDouble("longitude");
            return new Location(latitude, longitude);
        } else {
            return null;
        }
    }
}
