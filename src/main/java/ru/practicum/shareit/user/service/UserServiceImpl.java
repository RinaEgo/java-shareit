package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    UserMapper mapper = new UserMapper();

    public void validateUser(Integer id) {
        if (!userStorage.getUsersMap().containsKey(id)) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден.");
        }
    }

    public void validateEmailUniqueness(User user) {
        for (User existing : userStorage.findAllUsers()) {
            if (user.getEmail().equals(existing.getEmail())) {
                throw new ValidationException("Пользователь с таким email уже существует.");
            }
        }
    }

    @Override
    public UserDto getUserById(Integer userId) {
        validateUser(userId);
        User user = userStorage.getUserById(userId);

        return mapper.toUserDto(user);
    }

    @Override
    public List<UserDto> findAllUsers() {
        List<UserDto> users = new ArrayList<>();

        for (User user : userStorage.findAllUsers()) {
            users.add(mapper.toUserDto(user));
        }
        return users;
    }

    @Override
    public UserDto createUser(User user) {
        validateEmailUniqueness(user);
        return mapper.toUserDto(userStorage.createUser(user));
    }

    @Override
    public UserDto updateUser(User user, Integer userId) {
        validateUser(userId);
        User newUser = userStorage.getUserById(userId);

        if (user.getEmail() != null) {
            if (user.getEmail().equals(newUser.getEmail())) {
                newUser.setEmail(user.getEmail());
            } else {
                validateEmailUniqueness(user);
                newUser.setEmail(user.getEmail());
            }
        }

        if (user.getName() != null) {
            newUser.setName(user.getName());
        }

        return mapper.toUserDto(userStorage.updateUser(newUser));
    }

    @Override
    public void deleteUser(Integer userId) {
        validateUser(userId);
        userStorage.deleteUser(userId);
    }
}
