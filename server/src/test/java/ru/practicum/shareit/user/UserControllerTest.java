package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ErrorResponse;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;

    private static final long id = 1L;

    @Test
    void testCreateUser() throws Exception {
        UserDto userDto = new UserDto(id, "One", "one@gmail.com");
        when(userService.createUser(any())).thenReturn(userDto);
        mockMvc.perform(
                        post("/users")
                                .content(mapper.writeValueAsString(userDto))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("One"))
                .andExpect(jsonPath("$.email").value("one@gmail.com"));
    }

    @Test
    void testUpdateUser() throws Exception {
        UserDto userDto = new UserDto(id, "One", "one@gmail.com");
        when(userService.updateUser(any(), anyLong())).thenReturn(userDto);

        mockMvc.perform(
                        patch("/users/{userId}", id)
                                .content(mapper.writeValueAsString(userDto))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));
    }

    @Test
    void testGetUserById() throws Exception {
        UserDto userDto = new UserDto(1L, "One", "one@gmail.com");
        when(userService.getUserById(any())).thenReturn(userDto);

        mockMvc.perform(get("/users/{userId}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));
    }

    @Test
    void testDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(id);
        mockMvc.perform(delete("/users/{userId}", id))
                .andExpect(status().isOk());
    }

    @Test
    void testFindAllUsers() throws Exception {
        List<UserDto> users = new ArrayList<>(
                Arrays.asList(new UserDto(),
                        new UserDto()));
        when(userService.findAllUsers()).thenReturn(users);
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(users.size()));
    }

    @Test
    void shouldCreateErrorResponse() {
        String errorMessage = "Error";
        ErrorResponse error = new ErrorResponse(errorMessage);

        assertEquals(errorMessage, error.getError(), "Сообщение должно совпадать.");
    }
}
