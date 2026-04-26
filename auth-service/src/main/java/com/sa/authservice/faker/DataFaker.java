//package com.sa.authservice.faker;
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
//    CommandLineRunner seedDatabase(RoleSeeder roleSeeder, UserSeeder userSeeder) {
//        return args -> {
//            System.out.println("=== START SEED DATABASE ===");
//
//            roleSeeder.seed();
//            userSeeder.seed();
//
//            System.out.println("=== END SEED DATABASE ===");
//        };
//    }
//}
