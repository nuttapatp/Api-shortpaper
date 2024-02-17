package org.example.controller;

import org.example.LineNotifier;
import org.example.Main;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")

public class NotificationController {

    @PostMapping("/notify/{userId}")
    public ResponseEntity<String> sendNotificationToUser(@PathVariable String userId, @RequestBody String message) {
        System.out.println("ok , it is succed");
        System.out.println(userId);
        Main.sendNotificationToUser(userId, message);
        return ResponseEntity.ok("Notification sent successfully to user " + userId);
    }
}
