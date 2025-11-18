package com.Enigmazer.todo_app.service.category;

import com.Enigmazer.todo_app.dto.category.CategoryCreationRequest;
import com.Enigmazer.todo_app.dto.category.CategoryResponseDTO;
import com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException;
import com.Enigmazer.todo_app.mapper.CategoryMapper;
import com.Enigmazer.todo_app.model.Category;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.repository.CategoryRepository;
import com.Enigmazer.todo_app.service.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private JWTService jwtService;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private User mockUser;
    private Category mockCategory;
    private CategoryResponseDTO mockResponse;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).build();

        mockCategory = Category.builder().id(10L).name("Work").user(mockUser).build();

        mockResponse = CategoryResponseDTO.builder().id(10L).name("Work").build();

        when(jwtService.getCurrentUser()).thenReturn(mockUser);
    }

    @Test
    void createCategory_ShouldSaveAndReturnDto() {
        CategoryCreationRequest request = new CategoryCreationRequest("Work");

        when(categoryRepository.save(any(Category.class))).thenReturn(mockCategory);
        when(categoryMapper.toDto(mockCategory)).thenReturn(mockResponse);

        CategoryResponseDTO result = categoryService.createCategory(request);

        assertThat(result.getName()).isEqualTo("Work");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void getCategories_ShouldReturnMappedList() {
        when(categoryRepository.findAvailableCategories(mockUser.getId()))
                .thenReturn(List.of(mockCategory));
        when(categoryMapper.toDto(mockCategory)).thenReturn(mockResponse);

        List<CategoryResponseDTO> result = categoryService.getCategories();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Work");
    }

    @Test
    void getCategoryById_ShouldReturnCategory() {
        when(categoryRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(mockCategory));

        Category result = categoryService.getCategoryById(10L);

        assertThat(result.getName()).isEqualTo("Work");
    }

    @Test
    void getCategoryById_ShouldThrow_WhenNotFound() {
        when(categoryRepository.findByIdAndUserId(99L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCategory_ShouldUpdateNonGlobal() {
        CategoryCreationRequest req = new CategoryCreationRequest("Updated");

        when(categoryRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(mockCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(mockCategory);
        when(categoryMapper.toDto(mockCategory)).thenReturn(mockResponse);

        CategoryResponseDTO result = categoryService.updateCategory(10L, req);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldThrow_WhenGlobal() {
        mockCategory.setGlobal(true);
        when(categoryRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(mockCategory));

        CategoryCreationRequest req = new CategoryCreationRequest("Updated");

        assertThatThrownBy(() -> categoryService.updateCategory(10L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Global categories");
    }

    @Test
    void deleteCategory_ShouldThrow_WhenGlobal() {
        mockCategory.setGlobal(true);
        when(categoryRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(mockCategory));

        assertThatThrownBy(() -> categoryService.deleteCategory(10L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteCategory_ShouldThrow_WhenCategoryHasTasks() {
        when(categoryRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(mockCategory));
        when(categoryRepository.countTasksByCategoryId(10L)).thenReturn(1L);

        assertThatThrownBy(() -> categoryService.deleteCategory(10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("belongs to a task");
    }

    @Test
    void deleteCategory_ShouldDelete_WhenValid() {
        when(categoryRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(mockCategory));
        when(categoryRepository.countTasksByCategoryId(10L)).thenReturn(0L);

        categoryService.deleteCategory(10L);

        verify(categoryRepository, times(1)).delete(mockCategory);
    }

    @Test
    void countCategories_ShouldReturnCount() {
        when(categoryRepository.countCategories(mockUser.getId())).thenReturn(3);

        Integer count = categoryService.countCategories();

        assertThat(count).isEqualTo(3);
    }
}
