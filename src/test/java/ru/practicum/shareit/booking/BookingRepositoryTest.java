package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    private User owner;
    private User booker;
    private Booking booking;
    private Item item;
    private final LocalDateTime start = LocalDateTime.parse("2100-09-01T01:00");
    private final LocalDateTime end = LocalDateTime.parse("2110-09-01T01:00");

    @BeforeEach
    void setUp() {
        owner = new User("owner", "owner@gmail.com");
        booker = new User("booker", "boker@gmail.com");
        item = new Item("item", "item description", true, owner, null);

        owner = userRepository.save(owner);
        booker = userRepository.save(booker);
        item = itemRepository.save(item);

        booking = new Booking(1L, start, end, item, booker, Status.WAITING);
    }

    @Test
    void testFindAllByBooker() {
        booking = bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByBooker(booker, PageRequest.of(0, 2));
        assertThat(bookings).hasSize(1).contains(booking);
    }

    @Test
    void testFindAllByBookerAndStartBeforeAndEndAfter() {
        booking.setStart(LocalDateTime.parse("1900-09-01T01:00"));
        booking.setEnd(LocalDateTime.parse("3000-09-01T01:00"));
        booking = bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByBookerAndStartBeforeAndEndAfter(booker, LocalDateTime.now(), LocalDateTime.now(),
                PageRequest.of(0, 2));
        assertThat(bookings).hasSize(1).contains(booking);
    }

    @Test
    void testFindAllByBookerAndEndBefore() {
        booking.setStart(LocalDateTime.parse("1900-09-01T01:00"));
        booking.setEnd(LocalDateTime.parse("2000-09-01T01:00"));
        booking = bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByBookerAndEndBefore(booker, LocalDateTime.now(),
                PageRequest.of(0, 2));
        assertThat(bookings).hasSize(1).contains(booking);
    }

    @Test
    void testFindAllByBookerAndStartAfter() {
        booking.setStart(LocalDateTime.parse("2500-09-01T01:00"));
        booking.setEnd(LocalDateTime.parse("2600-09-01T01:00"));
        booking = bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByBookerAndStartAfter(booker, LocalDateTime.now(),
                PageRequest.of(0, 2));
        assertThat(bookings).hasSize(1).contains(booking);
    }

    @Test
    void testFindAllByBookerAndStatusEquals() {
        booking = bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByBookerAndStatusEquals(booker, Status.WAITING,
                PageRequest.of(0, 2));
        assertThat(bookings).hasSize(1).contains(booking);
    }

    @Test
    void testFindAllByItemOwner() {
        booking = bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByItemOwner(owner,
                PageRequest.of(0, 2));
        assertThat(bookings).hasSize(1).contains(booking);
    }

    @Test
    void testFindAllByItemOwnerAndStartBeforeAndEndAfter() {
        booking.setStart(LocalDateTime.parse("1900-09-01T01:00"));
        booking.setEnd(LocalDateTime.parse("2600-09-01T01:00"));
        booking = bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByItemOwnerAndStartBeforeAndEndAfter(owner, LocalDateTime.now(), LocalDateTime.now(),
                PageRequest.of(0, 2));
        assertThat(bookings).hasSize(1).contains(booking);
    }

    @Test
    void testFindAllByItemOwnerAndEndBefore() {
        booking.setStart(LocalDateTime.parse("1900-09-01T01:00"));
        booking.setEnd(LocalDateTime.parse("2000-09-01T01:00"));
        booking = bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByItemOwnerAndEndBefore(owner, LocalDateTime.now(),
                PageRequest.of(0, 2));
        assertThat(bookings).hasSize(1).contains(booking);
    }

    @Test
    void testFindAllByItemOwnerAndStartAfter() {
        booking.setStart(LocalDateTime.parse("3000-09-01T01:00"));
        booking.setEnd(LocalDateTime.parse("3500-09-01T01:00"));
        booking = bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByItemOwnerAndStartAfter(owner, LocalDateTime.now(),
                PageRequest.of(0, 2));
        assertThat(bookings).hasSize(1).contains(booking);
    }

    @Test
    void testFindAllByItemOwnerAndStatusEquals() {
        booking = bookingRepository.save(booking);

        List<Booking> bookings = bookingRepository.findAllByItemOwnerAndStatusEquals(owner, Status.WAITING,
                PageRequest.of(0, 2));
        assertThat(bookings).hasSize(1).contains(booking);
    }

    @Test
    void testFindBookingByItemWithDateBefore() {
        booking.setStart(LocalDateTime.parse("1000-09-01T01:00"));
        booking.setEnd(LocalDateTime.parse("3500-09-01T01:00"));
        booking = bookingRepository.save(booking);

        assertEquals(booking, bookingRepository.findBookingByItemWithDateBefore(item.getId(), LocalDateTime.now()));
    }

    @Test
    void testFindBookingByItemWithDateAfter() {
        booking.setStart(LocalDateTime.parse("2500-09-01T01:00"));
        booking.setEnd(LocalDateTime.parse("3500-09-01T01:00"));
        booking = bookingRepository.save(booking);

        assertEquals(booking, bookingRepository.findBookingByItemWithDateAfter(item.getId(), LocalDateTime.now()));
    }
}
