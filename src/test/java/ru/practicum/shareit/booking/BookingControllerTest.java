package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @MockBean
    private BookingService service;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;

    private BookingDto bookingDto;
    private BookingCreationDto bookingCreationDto;
    private final LocalDateTime start = LocalDateTime.parse("2100-09-01T01:00");
    private final LocalDateTime end = LocalDateTime.parse("2110-09-01T01:00");


    @BeforeEach
    void setUp() {
        bookingDto = new BookingDto(1L, start, end, null, null, Status.WAITING);
        bookingCreationDto = new BookingCreationDto(1L, start, end, null, null);
    }

    @Test
    void testApproveBooking() throws Exception {
        bookingDto.setStatus(Status.APPROVED);
        when(service.responseByOwner(1L, 1L, true)).thenReturn(bookingDto);

        mockMvc.perform(
                        patch("/bookings/{bookingId}", 1L)
                                .param("approved", "true")
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.start").value("2100-09-01T01:00:00"))
                .andExpect(jsonPath("$.end").value("2110-09-01T01:00:00"))
                .andExpect(jsonPath("$.status").value(Status.APPROVED.toString()));
    }

    @Test
    void testGetBooking() throws Exception {
        when(service.getBookingById(1L, 1L)).thenReturn(bookingDto);

        mockMvc.perform(
                        get("/bookings/{bookingId}", 1L)
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.start").value("2100-09-01T01:00:00"))
                .andExpect(jsonPath("$.end").value("2110-09-01T01:00:00"))
                .andExpect(jsonPath("$.status").value(Status.WAITING.toString()));
    }

    @Test
    void testGetAllBookingsByUserId() throws Exception {
        List<BookingDto> bookings = List.of(bookingDto);
        when(service.getAllBookingsByUserId(anyLong(), any(), anyInt(), anyInt())).thenReturn(bookings);

        mockMvc.perform(
                        get("/bookings")
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(bookings.size()));
    }

    @Test
    void testGetAllBookingsByOwnerId() throws Exception {
        List<BookingDto> bookings = List.of(bookingDto);
        when(service.getAllBookingsByOwnerId(anyLong(), any(), anyInt(), anyInt())).thenReturn(bookings);

        mockMvc.perform(
                        get("/bookings/owner")
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(bookings.size()));
    }
}