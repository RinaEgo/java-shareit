package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(properties = {"db.name=test"})
class ItemServiceImplTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;
    private ItemDto itemDto;
    private UserDto userDto;
    private UserDto createUserDto;
    private Item item;

    private final UserMapper userMapper = new UserMapper();
    private final ItemMapper itemMapper = new ItemMapper();

    @BeforeEach
    void setUp() {
        createUserDto = new UserDto("user", "user@gmail.com");
        userDto = userService.createUser(createUserDto);
        itemDto = new ItemDto("item1", "item description", true, null);
        item = itemMapper.toItem(itemService.createItem(itemDto, userDto.getId()));
    }

    @Test
    void testCreateItem() {
        assertThat(itemDto.getName()).as("Имя некорректно.").isEqualTo("item1");
        assertThat(itemDto.getDescription()).as("Описание некорректно.").isEqualTo("item description");
        assertThat(itemDto.getAvailable()).as("Статус доступа некорректен.").isTrue();
        assertThat(itemDto.getRequestId()).as("Id запросов должен быть пуст.").isNull();
    }

    @Test
    void testUpdateItemWithName() {
        ItemDto updatedItem = new ItemDto();
        updatedItem.setName("updated name");

        assertEquals("updated name", itemService.updateItem(updatedItem, item.getId(), userDto.getId()).getName(),
                "Обновление имени произведено некорректно.");
    }

    @Test
    void testUpdateItemWithDescription() {
        ItemDto updatedItem = new ItemDto();
        updatedItem.setDescription("updated description");

        assertEquals("updated description", itemService.updateItem(updatedItem, item.getId(), userDto.getId()).getDescription(),
                "Обновление описания произведено некорректно.");
    }

    @Test
    void testUpdateItemWithAvailableTrue() {
        ItemDto updatedItem = new ItemDto();
        updatedItem.setAvailable(false);

        assertFalse(itemService.updateItem(updatedItem, item.getId(), userDto.getId()).getAvailable(),
                "Обновление доступа произведено некорректно.");
    }

    @Test
    void testUpdateItemWithUserNotOwner() {
        ItemDto updatedItem = new ItemDto();
        updatedItem.setName("updated name");

        UserDto userNotOwner = new UserDto("notOwner", "notOwner@gmail.com");
        UserDto userDtoNotOwner = userService.createUser(userNotOwner);
        long idUserNotOwner = userDtoNotOwner.getId();

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(updatedItem, item.getId(), idUserNotOwner), "Исключение не выброшено.");
        assertTrue(ex.getMessage().contains("Пользователь с id: " + idUserNotOwner + "не является владельцем предмета."),
                "Сообщение не совпадает.");
    }

    @Test
    void testGetItemByIdWithOwner() {
        User user = userMapper.toUser(userDto);
        ItemDto itemDto = itemMapper.toItemDto(item);
        itemDto.setComments(new ArrayList<>());

        assertEquals(itemDto, itemService.getItemById(itemDto.getId(), userDto.getId()), "Поиск произведен некорректно.");
    }

    @Test
    void testGetItemByIdWithNotOwner() {
        UserDto userNotOwner = new UserDto("notOwner", "notOwner@gmail.com");
        userNotOwner = userService.createUser(userNotOwner);

        assertEquals(itemMapper.toItemDto(item), itemService.getItemById(item.getId(), userNotOwner.getId()),
                "Поиск произведен некорректно.");
    }

    @Test
    void testFindAllItemsOfOwner() {
        List<ItemDto> itemDtoList = new ArrayList<>();
        itemDtoList.add(itemMapper.toItemDto(item));

        assertEquals(itemDtoList, itemService.findAllItems(userDto.getId(), 0, 2), "Поиск произведен некорректно.");
    }

    @Test
    void testCreateComment() {
        UserDto author = new UserDto("author", "author@gmail.com");
        UserDto authorDto = userService.createUser(author);

        CommentDto commentDto = new CommentDto("comment", authorDto.getName(), null);

        LocalDateTime start = LocalDateTime.parse("1100-09-01T01:00");
        LocalDateTime end = LocalDateTime.parse("1200-09-01T01:00");

        BookingCreationDto bookingCreationDto = new BookingCreationDto(start, end, item.getId());
        BookingDto createdBooking = bookingService.createBooking(bookingCreationDto, authorDto.getId());

        bookingService.responseByOwner(createdBooking.getId(), userDto.getId(), true);
        CommentDto result = itemService.createComment(item.getId(), authorDto.getId(), commentDto);

        assertThat(result.getText()).as("Текст комментария некорректен.").isEqualTo("comment");
        assertThat(result.getAuthorName()).as("Имя автора некорректно.").isEqualTo(authorDto.getName());
    }

    @Test
    void testCreateCommentWithNoBooking() {
        CommentDto commentDto = new CommentDto(1L, "comment", "author", null);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> itemService.createComment(item.getId(), userDto.getId(), commentDto));
        assertThat(ex.getMessage()).as("Сообщения должны совпадать.")
                .contains("Предмет не бронировался пользователем с id " + userDto.getId() +
                " или аренда не завершена. Доступ к комментированию предмета закрыт.");
    }

    @Test
    void testSearchWithBlankList() {
        assertEquals(Collections.EMPTY_LIST, itemService.search("", 0, 2),
                "Список должен быть пуст.");
    }

    @Test
    void testSearch() {
        assertEquals(1,
                itemService.search("ite", 0, 2).size(), "Поиск работает некорректно.");
    }
}
