package com.contrimate.contrimate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ContrimateApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContrimateApplication.class, args);
    }
    
    // Yahan neeche kuch nahi hona chahiye!
    // Agar koi @Bean public JavaMailSender... dikhe toh delete kar do.
}