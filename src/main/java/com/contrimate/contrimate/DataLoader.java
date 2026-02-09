package com.contrimate.contrimate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        // Bhai ab yahan sannata hai. 
        // Koi Goa Trip nahi, koi purana data nahi.
        System.out.println("✅ FRESH START: No dummy data loaded.");
    }
}