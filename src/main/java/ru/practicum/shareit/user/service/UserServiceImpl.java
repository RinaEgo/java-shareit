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

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    UserMapper mapper = new UserMapper();

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
    public UserDto createUser(User user) {
        return mapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserDto updateUser(User user, Long userId) {

        User newUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        if (user.getEmail() != null) {
            if (user.getEmail().equals(newUser.getEmail())) {
                newUser.setEmail(user.getEmail());
            } else {
                newUser.setEmail(user.getEmail());
            }
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
