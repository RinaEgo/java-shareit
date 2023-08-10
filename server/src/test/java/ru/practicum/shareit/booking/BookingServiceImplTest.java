package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
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
class BookingServiceImplTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    private Item item;
    private UserDto bookerDto;
    private UserDto ownerDto;
    private UserDto anotherUserDto;
    private User booker;
    private User owner;
    private User anotherUser;
    private BookingDto bookingDto;
    private BookingCreationDto bookingCreationDto;
    private final UserMapper userMapper = new UserMapper();
    private final ItemMapper itemMapper = new ItemMapper();
    private final LocalDateTime start = LocalDateTime.parse("2100-09-01T01:00");
    private final LocalDateTime end = LocalDateTime.parse("2110-09-01T01:00");

    @BeforeEach
    void setUp() {
        ownerDto = new UserDto("owner", "owner@gmail.com");
        bookerDto = new UserDto("booker", "booker@gmail.com");
        anotherUserDto = new UserDto("another", "another@gmail.com");
        owner = userMapper.toUser(userService.createUser(ownerDto));
        booker = userMapper.toUser(userService.createUser(bookerDto));
        anotherUser = userMapper.toUser(userService.createUser(anotherUserDto));

        item = new Item("item", "item description", true, owner, null);
        item = itemMapper.toItem(itemService.createItem(itemMapper.toItemDto(item), owner.getId()));

        bookingCreationDto = new BookingCreationDto(start, end, item.getId());
        bookingDto = bookingService.createBooking(bookingCreationDto, booker.getId());
    }

    @Test
    void testCreateBooking() {
        assertThat(bookingDto.getId()).as("Поле Id не может быть пустым").isNotZero();
        assertThat(bookingDto.getBooker().getId()).as("Id арендатора должно совпадать.").isEqualTo(booker.getId());
        assertThat(bookingDto.getBooker().getName()).as("Имя арендатора должно совпадать.").isEqualTo(booker.getName());
        assertThat(bookingDto.getStart()).as("Дата начала должна совпадать.").isEqualTo(start);
        assertThat(bookingDto.getEnd()).as("Дата окончания должна совпадать.").isEqualTo(end);
        assertThat(bookingDto.getItem().getId()).as("Id предмета должно совпадать.").isEqualTo(item.getId());
        assertThat(bookingDto.getItem().getName()).as("Название предмета должно совпадать.").isEqualTo(item.getName());
        assertThat(bookingDto.getStatus()).as("Статус должен совпадать.").isEqualTo(Status.WAITING);
    }

    @Test
    void testCreateBookingWithWrongDate() {
        LocalDateTime start = LocalDateTime.parse("2000-09-01T01:00");
        LocalDateTime end = LocalDateTime.parse("1000-09-01T01:00");
        bookingCreationDto.setStart(start);
        bookingCreationDto.setEnd(end);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(bookingCreationDto, booker.getId()));
        assertThat(ex.getMessage()).as("Сообщение должно совпадать.")
                .contains("Дата окончания бронирования раньше или равно дате начала. Бронирование невозможно.");
    }

    @Test
    void testCreateBookingWithStatusNotAvailable() {
        item.setAvailable(false);
        itemService.createItem(itemMapper.toItemDto(item), anotherUser.getId());
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(bookingCreationDto, booker.getId()));
        assertThat(ex.getMessage()).as("Сообщение должно совпадать.")
                .contains("Предмет недоступен. Бронирование невозможно.");
    }

    @Test
    void testCreateBookingWhenBookerIsOwner() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookingCreationDto, owner.getId()));
        assertThat(ex.getMessage()).as("Сообщение должно совпадать.")
                .contains("Предмет принадлежит пользователю. Бронирование невозможно.");
    }

    @Test
    void testApproveBooking() {
        bookingDto.setStatus(Status.APPROVED);
        assertEquals(bookingDto, bookingService.responseByOwner(bookingDto.getId(), owner.getId(), true),
                "Ошибка при подтверждении брони.");
    }

    @Test
    void testGetBookingById() {
        assertEquals(bookingDto, bookingService.getBookingById(bookingDto.getId(), owner.getId()),
                "Ошибка при поиске по ID");
    }

    @Test
    void testGetBookingByIdWithUserNotFound() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(-1L, owner.getId()));
        assertThat(ex.getMessage()).as("Сообщение должно совпадать.")
                .contains("Бронирование с id -1 не найдено.");
    }

    @Test
    void testGetBookingByIdWithBookingNotFound() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(bookingDto.getId(), anotherUser.getId()));
        assertThat(ex.getMessage()).as("Сообщение должно совпадать.")
                .contains("Доступ невозможен. Пользователь не является хозяином вещи или автором брони.");
    }

    @Test
    void testGetAllBookingsByUserIdWithUserNotFound() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.getAllBookingsByUserId(-1L, "ALL", 0, 2));
        assertThat(ex.getMessage()).as("Сообщение должно совпадать.")
                .contains("Пользователь с ID " + -1L + " не найден.");
    }

    @Test
    void testGetAllBookingsByUserIdWithAllState() {
        List<BookingDto> bookings = List.of(bookingDto);
        assertEquals(bookings, bookingService.getAllBookingsByUserId(booker.getId(), "ALL", 0, 2),
                "Ошибка при поиске по ID пользователя, ALL");
    }

    @Test
    void testGetAllBookingsByUserIdWithCurrentState() {
        LocalDateTime start = LocalDateTime.parse("1000-09-01T01:00");
        LocalDateTime end = LocalDateTime.parse("2100-09-01T01:00");
        bookingCreationDto = new BookingCreationDto(start, end, item.getId());
        bookingDto = bookingService.createBooking(bookingCreationDto, booker.getId());
        List<BookingDto> bookings = List.of(bookingDto);
        assertEquals(bookings, bookingService.getAllBookingsByUserId(booker.getId(), "CURRENT", 0, 2),
                "Ошибка при поиске по ID пользователя, CURRENT");
    }

    @Test
    void testGetAllBookingsByUserIdWithPastState() {
        LocalDateTime start = LocalDateTime.parse("1000-09-01T01:00");
        LocalDateTime end = LocalDateTime.parse("1500-09-01T01:00");
        bookingCreationDto = new BookingCreationDto(start, end, item.getId());
        bookingDto = bookingService.createBooking(bookingCreationDto, booker.getId());
        List<BookingDto> bookings = List.of(bookingDto);
        assertEquals(bookings, bookingService.getAllBookingsByUserId(booker.getId(), "PAST", 0, 2),
                "Ошибка при поиске по ID пользователя, PAST");
    }

    @Test
    void testGetAllBookingsByUserIdWithFutureState() {
        List<BookingDto> bookings = List.of(bookingDto);
        assertEquals(bookings, bookingService.getAllBookingsByUserId(booker.getId(), "FUTURE", 0, 2),
                "Ошибка при поиске по ID пользователя, FUTURE");
    }

    @Test
    void testGetAllBookingsByUserIdWithWaitingStatus() {
        List<BookingDto> bookings = List.of(bookingDto);
        assertEquals(bookings, bookingService.getAllBookingsByUserId(booker.getId(), "WAITING", 0, 2),
                "Ошибка при поиске по ID пользователя, WAITING");
    }

    @Test
    void testGetAllBookingsByUserIdWithRejectedStatus() {
        bookingDto = bookingService.responseByOwner(bookingDto.getId(), owner.getId(), false);
        List<BookingDto> bookings = List.of(bookingDto);
        assertEquals(bookings, bookingService.getAllBookingsByUserId(booker.getId(), "REJECTED", 0, 2),
                "Ошибка при поиске по ID пользователя, REJECTED");
    }

    @Test
    void testGetAllBookingsByUserIdWithUnsupportedState() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.getAllBookingsByUserId(booker.getId(),
                        "UNSUPPORTED_STATUS", 0, 2));
        assertThat(ex.getMessage()).as("Сообщение должно совпадать.")
                .contains("Unknown state: UNSUPPORTED_STATUS");
    }

    @Test
    void testGetAllBookingsByOwnerIdWithOwnerNotFound() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.getAllBookingsByOwnerId(-1L, "ALL", 0, 2));
        assertThat(ex.getMessage()).as("Сообщение должно совпадать.")
                .contains("Пользователь с ID " + -1L + " не найден.");
    }

    @Test
    void testGetAllBookingsByOwnerIdWithAllState() {
        List<BookingDto> bookings = List.of(bookingDto);
        assertEquals(bookings, bookingService.getAllBookingsByOwnerId(owner.getId(), "ALL", 0, 2),
                "Ошибка при поиске по ID владельца, ALL");
    }

    @Test
    void testGetAllBookingsByOwnerIdWithCurrentState() {
        LocalDateTime start = LocalDateTime.parse("1000-09-01T01:00");
        LocalDateTime end = LocalDateTime.parse("2100-09-01T01:00");
        bookingCreationDto = new BookingCreationDto(start, end, item.getId());
        bookingDto = bookingService.createBooking(bookingCreationDto, booker.getId());
        List<BookingDto> bookings = List.of(bookingDto);
        assertEquals(bookings, bookingService.getAllBookingsByOwnerId(owner.getId(), "CURRENT", 0, 2),
                "Ошибка при поиске по ID владельца, CURRENT");
    }

    @Test
    void testGetAllBookingsByOwnerIdWithPastState() {
        LocalDateTime start = LocalDateTime.parse("1000-09-01T01:00");
        LocalDateTime end = LocalDateTime.parse("1500-09-01T01:00");
        bookingCreationDto = new BookingCreationDto(start, end, item.getId());
        bookingDto = bookingService.createBooking(bookingCreationDto, booker.getId());
        List<BookingDto> bookings = List.of(bookingDto);
        assertEquals(bookings, bookingService.getAllBookingsByOwnerId(owner.getId(), "PAST", 0, 2),
                "Ошибка при поиске по ID владельца, PAST");
    }

    @Test
    void testGetAllBookingsByOwnerIdWithFutureState() {
        List<BookingDto> bookings = List.of(bookingDto);
        assertEquals(bookings, bookingService.getAllBookingsByOwnerId(owner.getId(), "FUTURE", 0, 2),
                "Ошибка при поиске по ID владельца, FUTURE");
    }

    @Test
    void testGetAllBookingsByOwnerIdWithWaitingStatus() {
        List<BookingDto> bookings = List.of(bookingDto);
        assertEquals(bookings, bookingService.getAllBookingsByOwnerId(owner.getId(), "WAITING", 0, 2),
                "Ошибка при поиске по ID владельца, WAITING");
    }

    @Test
    void testGetAllBookingsByOwnerIdWithRejectedStatus() {
        bookingDto = bookingService.responseByOwner(bookingDto.getId(), owner.getId(), false);
        List<BookingDto> bookings = List.of(bookingDto);
        assertEquals(bookings, bookingService.getAllBookingsByOwnerId(owner.getId(), "REJECTED", 0, 2),
                "Ошибка при поиске по ID владельца, REJECTED");
    }

    @Test
    void testGetAllBookingsByOwnerIdWithUnsupportedState() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.getAllBookingsByOwnerId(owner.getId(),
                        "UNSUPPORTED_STATUS", 0, 2));
        assertThat(ex.getMessage()).as("Сообщение должно совпадать.")
                .contains("Unknown state: UNSUPPORTED_STATUS");
    }
}
