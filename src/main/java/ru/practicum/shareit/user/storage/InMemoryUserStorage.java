package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int userId = 1;

    @Override
    public Map<Integer, User> getUsersMap() {
        return users;
    }

    @Override
    public User getUserById(Integer userId) {
        if (users.containsKey(userId)) {
            return users.get(userId);
        } else {
            log.warn("Пользователь не был зарегистрирован.");
            throw new NotFoundException("Пользователь не был зарегистрирован.");
        }
    }

    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User createUser(User user) {
        if (users.containsKey(user.getId())) {
            log.warn("Пользователь уже существует.");
            throw new ValidationException("Пользователь уже существует.");
        } else {
            user.setId(userId);
            users.put(user.getId(), user);
            userId++;
            log.info("Пользователь {} добавлен.", user);
        }
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.info("Пользователь {} добавлен.", user);
        } else {
            log.warn("Пользователь не был зарегистрирован.");
            throw new NotFoundException("Пользователь не был зарегистрирован.");
        }
        return user;
    }

    @Override
    public void deleteUser(Integer userId) {
        if (users.containsKey(userId)) {
            log.info("Пользователь {} удален.", getUserById(userId));
            users.remove(userId);
        } else {
            log.warn("Пользователь не был зарегистрирован.");
            throw new NotFoundException("Пользователь не был зарегистрирован.");
        }
    }
}
