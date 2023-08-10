package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(properties = {"db.name=test"})
class UserServiceImplTest {

    @Autowired
    private UserService userService;
    private UserDto userDto;
    private UserDto createUser;
    private final UserMapper userMapper = new UserMapper();

    @BeforeEach
    void setUp() {
        createUser = new UserDto("user1", "user1@gmail.com");
        userDto = userService.createUser(createUser);
    }

    @Test
    void testCreateUser() {
        assertThat(userDto.getName()).isEqualTo("user1");
        assertThat(userDto.getEmail()).isEqualTo("user1@gmail.com");
    }

    @Test
    void testUpdateUser() {
        UserDto updated = new UserDto();
        updated.setEmail("newEmail@gmail.com");
        userDto.setEmail("newEmail@gmail.com");

        assertEquals(userDto, userService.updateUser(updated, userDto.getId()));
    }

    @Test
    void testUpdateUserNotFound() {
        UserDto anotherUser = new UserDto("another", "another@gmail.com");
        userService.createUser(anotherUser);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.updateUser(anotherUser, 1000L));
        assertThat(ex.getMessage()).contains("Пользователь с ID " + 1000L + " не найден.");
    }

    @Test
    void testGetUserById() {
        assertEquals(userDto, userService.getUserById(userDto.getId()));
    }

    @Test
    void testFindAllUsers() {
        List<UserDto> userList = List.of(userDto);
        assertEquals(userList, userService.findAllUsers());
    }
}
