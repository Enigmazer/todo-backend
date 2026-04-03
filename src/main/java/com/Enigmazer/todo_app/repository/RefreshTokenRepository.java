package com.Enigmazer.todo_app.repository;

import com.Enigmazer.todo_app.model.RefreshToken;
import com.Enigmazer.todo_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);


    @Query("SELECT r.user FROM RefreshToken r WHERE r.token = :token")
    Optional<User> findUserByToken(@Param("token") String token);

    Optional<RefreshToken> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByToken(String token);

    void deleteByUser(User user);
}