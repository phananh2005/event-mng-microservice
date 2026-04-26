//package com.sa.bookingservice.faker;
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
//    CommandLineRunner seedDatabase(CartSeeder cartSeeder, OrderSeeder orderSeeder) {
//        return args -> {
//            System.out.println("=== START SEED DATABASE ===");
//
//            cartSeeder.seed();
//            orderSeeder.seed();
//
//            System.out.println("=== END SEED DATABASE ===");
//        };
//    }
//}
