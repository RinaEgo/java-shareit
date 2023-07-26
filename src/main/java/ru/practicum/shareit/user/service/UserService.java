package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserDto getUserById(Long userId);

    List<UserDto> findAllUsers();

    UserDto createUser(User user);

    UserDto updateUser(User user, Long userId);

    void deleteUser(Long userId);
}
