package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Map;

public interface UserStorage {

    Map<Integer, User> getUsersMap();

    User getUserById(Integer userId);

    List<User> findAllUsers();

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(Integer userId);
}
