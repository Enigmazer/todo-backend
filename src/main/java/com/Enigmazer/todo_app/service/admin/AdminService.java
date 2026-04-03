package com.Enigmazer.todo_app.service.admin;

import com.Enigmazer.todo_app.dto.category.CategoryCreationRequest;
import com.Enigmazer.todo_app.dto.category.CategoryResponseDTO;
import com.Enigmazer.todo_app.dto.user.UserRegistrationRequest;
import com.Enigmazer.todo_app.model.User;

import java.util.List;

public interface AdminService {

    void createAdmin(UserRegistrationRequest user);

    void updateUserAccountStatus(long userId, boolean status);

    void deleteUser(long userId);

    List<User> getUsers();

    CategoryResponseDTO createGlobalCategory(CategoryCreationRequest category);

}
