package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByItemOwner(User owner, Pageable pageable);

    List<Booking> findAllByItemOwnerAndStartBeforeAndEndAfter(
            User owner,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable);

    List<Booking> findAllByItemOwnerAndEndBefore(User owner, LocalDateTime end, Pageable pageable);

    List<Booking> findAllByItemOwnerAndStartAfter(User owner, LocalDateTime start, Pageable pageable);

    List<Booking> findAllByItemOwnerAndStatusEquals(User owner, Status status, Pageable pageable);

    List<Booking> findAllByBooker(User booker, Pageable pageable);

    List<Booking> findAllByBookerAndStartBeforeAndEndAfter(
            User booker,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable);

    List<Booking> findAllByBookerAndEndBefore(User booker, LocalDateTime end, Pageable pageable);

    List<Booking> findAllByBookerAndStartAfter(User booker, LocalDateTime start, Pageable pageable);

    List<Booking> findAllByBookerAndStatusEquals(User booker, Status status, Pageable pageable);

    List<Booking> findAllByBookerIdAndItemIdAndStatusEqualsAndEndIsBefore(
            Long bookerId,
            Long itemId,
            Status status,
            LocalDateTime end);

    @Query(value = "select * from bookings b " +
            "where b.item_id = :itemId and b.start_date <= :date order by b.start_date desc limit 1",
            nativeQuery = true)
    Booking findBookingByItemWithDateBefore(@Param("itemId") long itemId, @Param("date") LocalDateTime date);

    @Query(value = "select * from bookings b " +
            "where b.item_id = :itemId and b.start_date >= :date order by b.start_date limit 1",
            nativeQuery = true)
    Booking findBookingByItemWithDateAfter(@Param("itemId") long itemId, @Param("date") LocalDateTime date);
}
