package com.sa.eventservice.service;

import com.sa.eventservice.dto.request.CategoryRequest;
import com.sa.eventservice.dto.response.CategoryResponse;
import com.sa.eventservice.exception.AppException;
import com.sa.eventservice.exception.ErrorCode;
import com.sa.eventservice.mapper.CategoryMapper;
import com.sa.eventservice.model.entity.Category;
import com.sa.eventservice.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void create_shouldPersistAndReturnMappedResponse() {
        CategoryRequest request = CategoryRequest.builder().name("Music").description("Music events").build();
        Category saved = Category.builder().id(1L).name("Music").description("Music events").build();
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Music").description("Music events").build();

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);
        when(categoryMapper.toCategoryResponse(saved)).thenReturn(response);

        CategoryResponse result = categoryService.create(request);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertEquals("Music", captor.getValue().getName());
        assertEquals("Music", result.getName());
    }

    @Test
    void getAll_shouldReturnMappedCategories() {
        Category category = Category.builder().id(1L).name("Music").description("Music events").build();
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Music").description("Music events").build();

        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryMapper.toCategoryResponse(category)).thenReturn(response);

        List<CategoryResponse> result = categoryService.getAll();

        assertEquals(1, result.size());
        assertEquals("Music", result.get(0).getName());
    }

    @Test
    void getById_shouldThrowAppExceptionWhenNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> categoryService.getById(999L));

        assertEquals(ErrorCode.CATEGORY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void update_shouldThrowAppExceptionWhenNotFound() {
        CategoryRequest request = CategoryRequest.builder().name("Cinema").description("Cinema events").build();
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> categoryService.update(999L, request));

        assertEquals(ErrorCode.CATEGORY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void update_shouldPersistAndReturnMappedResponse() {
        Category existing = Category.builder().id(1L).name("Music").description("Old").build();
        CategoryRequest request = CategoryRequest.builder().name("Cinema").description("Cinema events").build();
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Cinema").description("Cinema events").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);
        when(categoryMapper.toCategoryResponse(existing)).thenReturn(response);

        CategoryResponse result = categoryService.update(1L, request);

        assertEquals("Cinema", existing.getName());
        assertEquals("Cinema", result.getName());
    }

    @Test
    void delete_shouldCallRepository() {
        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }
}
