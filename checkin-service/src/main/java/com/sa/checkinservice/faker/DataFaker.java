//package com.sa.checkinservice.faker;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@RequiredArgsConstructor
//public class DataFaker {
//
//    @Bean
//    CommandLineRunner seedDatabase(CheckinLogSeeder checkinLogSeeder) {
//        return args -> {
//            System.out.println("=== START SEED DATABASE ===");
//
//            checkinLogSeeder.seed();
//
//            System.out.println("=== END SEED DATABASE ===");
//        };
//    }
//}
