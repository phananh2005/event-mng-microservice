//package com.sa.ticketservice.faker;
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
//    CommandLineRunner seedDatabase(TicketTypeSeeder ticketTypeSeeder, TicketSeeder ticketSeeder) {
//        return args -> {
//            System.out.println("=== START SEED DATABASE ===");
//
//            ticketTypeSeeder.seed();
//            ticketSeeder.seed();
//
//            System.out.println("=== END SEED DATABASE ===");
//        };
//    }
//}
