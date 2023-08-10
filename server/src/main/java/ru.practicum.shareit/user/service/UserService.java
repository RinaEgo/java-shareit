package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto getUserById(Long userId);

    List<UserDto> findAllUsers();

    UserDto createUser(UserDto user);

    UserDto updateUser(UserDto user, Long userId);

    void deleteUser(Long userId);
}
