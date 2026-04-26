//package com.sa.eventservice.faker;
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
//    CommandLineRunner seedDatabase(
//            CategorySeeder categorySeeder,
//            EventSeeder eventSeeder,
//            TicketTypeSeeder ticketTypeSeeder
//    ) {
//        return args -> {
//            System.out.println("=== START SEED DATABASE ===");
//
//            categorySeeder.seed();
//            eventSeeder.seed();
//            ticketTypeSeeder.seed();
//
//            System.out.println("=== END SEED DATABASE ===");
//        };
//    }
//}
