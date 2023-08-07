package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
class ItemRequestServiceImplTest {
    @Autowired
    private final ItemRequestService itemRequestService;
    @Autowired
    private final UserService userService;
    private ItemRequestDto itemRequestDto;
    private User requestor;
    private User requestorCreated;
    private final UserMapper userMapper = new UserMapper();

    @BeforeEach
    void setUp() {
        requestor = new User("requestor", "requestor@gmail.com");
        itemRequestDto = new ItemRequestDto("request description", 1L,
                LocalDateTime.parse("2100-09-01T01:00"), new ArrayList<>());
    }

    @Test
    void testCreateRequest() {
        requestorCreated = userMapper.toUser(userService.createUser(requestor));
        itemRequestDto = itemRequestService.createRequest(requestorCreated.getId(), itemRequestDto);

        assertEquals(itemRequestDto, itemRequestService.getRequestById(itemRequestDto.getId(), requestorCreated.getId()));
    }

    @Test
    void testFindAllRequestsByUser() {
        requestorCreated = userMapper.toUser(userService.createUser(requestor));
        itemRequestDto = itemRequestService.createRequest(requestorCreated.getId(), itemRequestDto);
        assertThat(itemRequestService.findAllRequestsByUser(requestorCreated.getId())).hasSize(1).contains(itemRequestDto);
    }

    @Test
    void testFindAllRequests() {
        requestorCreated = userMapper.toUser(userService.createUser(requestor));
        itemRequestDto = itemRequestService.createRequest(requestorCreated.getId(), itemRequestDto);
        List<ItemRequestDto> requests = itemRequestService.findAllRequests(requestorCreated.getId(), 0, 5);
        assertThat(requests).hasSize(0);
    }

    @Test
    void testRequestFields() {
        requestorCreated = userMapper.toUser(userService.createUser(requestor));
        itemRequestDto = itemRequestService.createRequest(requestorCreated.getId(), itemRequestDto);
        assertThat(itemRequestDto.getId()).isNotZero();
        assertThat(itemRequestDto.getDescription()).isEqualTo("request description");
        assertThat(itemRequestDto.getItems().size()).isZero();
        assertThat(itemRequestDto.getRequestorId()).isEqualTo(requestorCreated.getId());
    }

    @Test
    void testGetRequestByIdWithUserNotFound() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(requestor.getId(), 1000L));
        assertThat(ex.getMessage()).contains("Пользователь с ID " + 1000L + " не найден.");
    }
}
