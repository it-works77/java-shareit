package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingRequestState;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.EntityNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    void add_whenValid_returnsOk() throws Exception {
        when(bookingService.create(any(BookingCreateRequestDto.class)))
                .thenReturn(BookingResponseDto.builder().id(1L).build());

        mockMvc.perform(post("/bookings")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void add_whenDatesInverted_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":1,\"start\":\"2026-10-10T12:00:00\","
                                + "\"end\":\"2026-10-09T12:00:00\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateApprovement_whenValid_returnsOk() throws Exception {
        when(bookingService.updateApprovement(1L, 1L, true))
                .thenReturn(BookingResponseDto.builder().id(1L).build());

        mockMvc.perform(patch("/bookings/1").header(HEADER, 1L).param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateApprovement_whenNotOwner_returnsForbidden() throws Exception {
        when(bookingService.updateApprovement(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new AccessDeniedException("not owner"));

        mockMvc.perform(patch("/bookings/1").header(HEADER, 1L).param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_whenFound_returnsOk() throws Exception {
        when(bookingService.getByUserIdAndBookingId(1L, 1L))
                .thenReturn(BookingResponseDto.builder().id(1L).build());

        mockMvc.perform(get("/bookings/1").header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getById_whenNotFound_returnsNotFound() throws Exception {
        when(bookingService.getByUserIdAndBookingId(anyLong(), anyLong()))
                .thenThrow(new EntityNotFoundException("not found"));

        mockMvc.perform(get("/bookings/99").header(HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllByBooker_returnsOkWithDefaultState() throws Exception {
        when(bookingService.getAllByBookerIdAndState(1L, BookingRequestState.ALL))
                .thenReturn(List.of(BookingResponseDto.builder().id(1L).build()));

        mockMvc.perform(get("/bookings").header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getAllByBooker_whenStateUnknown_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings").header(HEADER, 1L).param("state", "NOPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllByOwner_returnsOk() throws Exception {
        when(bookingService.getAllByOwnerIdAndState(1L, BookingRequestState.WAITING))
                .thenReturn(List.of(BookingResponseDto.builder().id(2L).build()));

        mockMvc.perform(get("/bookings/owner").header(HEADER, 1L).param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L));
    }

    @Test
    void updateApprovement_whenApprovedMissing_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1").header(HEADER, 1L))
                .andExpect(status().isBadRequest());
    }

    private static String validCreateJson() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(2).withNano(0);
        return "{\"itemId\":1,\"start\":\"" + start + "\",\"end\":\"" + end + "\"}";
    }
}
