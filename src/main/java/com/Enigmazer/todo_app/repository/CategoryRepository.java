package com.Enigmazer.todo_app.repository;

import com.Enigmazer.todo_app.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
         SELECT c FROM Category c \s
         WHERE c.isGlobal = true OR c.user.id = :userId
        \s""")
    List<Category> findAvailableCategories(@Param("userId") Long userId);

    @Query("""
         SELECT c FROM Category c \s
         WHERE c.id = :id AND (c.isGlobal = true OR c.user.id = :userId)
        \s""")
    Optional<Category> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("""
         SELECT c FROM Category c \s
         WHERE c.name = :name AND c.isGlobal = true
        \s""")
    Optional<Category> findByNameAndGlobal(@Param("name") String name);

    @Query("""
         SELECT COUNT(t) FROM Task t\s
         WHERE t.category.id = :id
        \s""")
    Long countTasksByCategoryId(@Param("id") Long categoryId);

    @Query("SELECT count(c) FROM Category c WHERE c.user.id = :userId OR c.isGlobal = true")
    Integer totalCategoriesOfUser(@Param("userId") Long userId);
}
