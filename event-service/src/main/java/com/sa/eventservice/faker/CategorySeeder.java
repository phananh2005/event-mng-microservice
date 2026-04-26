package com.sa.eventservice.faker;

import com.sa.eventservice.model.entity.Category;
import com.sa.eventservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategorySeeder {

    private final CategoryRepository categoryRepository;

    public void seed() {
        if (categoryRepository.count() > 0) return;
        categoryRepository.saveAll(List.of(
                Category.builder().name("Music").description("Concerts and music festivals").build(),
                Category.builder().name("Technology").description("Tech conferences and hackathons").build(),
                Category.builder().name("Sports").description("Sporting events and tournaments").build(),
                Category.builder().name("Art & Culture").description("Exhibitions and cultural events").build(),
                Category.builder().name("Food & Drink").description("Food festivals and tastings").build()
        ));
    }
}
