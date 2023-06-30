package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserDto getUserById(Integer userId);

    List<UserDto> findAllUsers();

    UserDto createUser(User user);

    UserDto updateUser(User user, Integer userId);

    void deleteUser(Integer userId);
}
