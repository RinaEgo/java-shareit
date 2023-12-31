package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper mapper = new UserMapper();

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        return mapper.toUserDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> findAllUsers() {
        List<UserDto> users = new ArrayList<>();

        for (User user : userRepository.findAll()) {
            users.add(mapper.toUserDto(user));
        }
        return users;
    }

    @Transactional
    @Override
    public UserDto createUser(UserDto user) {
        return mapper.toUserDto(userRepository.save(mapper.toUser(user)));
    }

    @Transactional
    @Override
    public UserDto updateUser(UserDto user, Long userId) {

        User newUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        if (user.getEmail() != null) {
            newUser.setEmail(user.getEmail());
        }

        if (user.getName() != null) {
            newUser.setName(user.getName());
        }

        return mapper.toUserDto(userRepository.save(newUser));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
