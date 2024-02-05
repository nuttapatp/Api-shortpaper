package org.example.controller;
import org.example.Main;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class WarmupController {

    @GetMapping("/_ah/warmup")
    public String warmup() throws IOException {
//        Main.initializeFirebase();

        // Perform any initialization tasks here (e.g., connect to Firebase).
        return "Warm-up completed.";
    }
}
