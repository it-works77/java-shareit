package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingRequestState;


import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void addBooking_shouldSetBookerIdFromHeaderAndReturnCreatedBooking() throws Exception {
        BookingCreateRequestDto request = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingResponseDto response = BookingResponseDto.builder()
                .id(1L)
                .start(request.getStart())
                .end(request.getEnd())
                .build();

        when(bookingService.create(any(BookingCreateRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        ArgumentCaptor<BookingCreateRequestDto> captor = ArgumentCaptor.forClass(BookingCreateRequestDto.class);
        verify(bookingService).create(captor.capture());
        assertThat(captor.getValue().getBookerId()).isEqualTo(1L);
    }

    @Test
    void addBooking_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        BookingCreateRequestDto request = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void updateApprovement_shouldPassOwnerAndBookingAndApproved() throws Exception {
        BookingResponseDto response = BookingResponseDto.builder().id(1L).build();

        when(bookingService.updateApprovement(1L, 1L, true)).thenReturn(response);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(bookingService).updateApprovement(1L, 1L, true);
    }

    @Test
    void getByUserIdAndBookingId_shouldReturnBooking() throws Exception {
        BookingResponseDto response = BookingResponseDto.builder().id(1L).build();

        when(bookingService.getByUserIdAndBookingId(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(bookingService).getByUserIdAndBookingId(1L, 1L);
    }

    @Test
    void getAllByBookerIdAndState_shouldReturnListOfBookings() throws Exception {
        BookingResponseDto response = BookingResponseDto.builder().id(1L).build();

        when(bookingService.getAllByBookerIdAndState(1L, BookingRequestState.ALL))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(bookingService).getAllByBookerIdAndState(1L, BookingRequestState.ALL);
    }

    @Test
    void getAllByBookerIdAndState_shouldUseDefaultStateWhenParamAbsent() throws Exception {
        when(bookingService.getAllByBookerIdAndState(1L, BookingRequestState.ALL))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(bookingService).getAllByBookerIdAndState(1L, BookingRequestState.ALL);
    }

    @Test
    void getAllByOwnerIdAndState_shouldReturnListOfBookings() throws Exception {
        BookingResponseDto response = BookingResponseDto.builder().id(2L).build();

        when(bookingService.getAllByOwnerIdAndState(1L, BookingRequestState.CURRENT))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "CURRENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L));

        verify(bookingService).getAllByOwnerIdAndState(1L, BookingRequestState.CURRENT);
    }

    @Test
    void getAllByOwnerIdAndState_shouldUseDefaultStateWhenParamAbsent() throws Exception {
        when(bookingService.getAllByOwnerIdAndState(1L, BookingRequestState.ALL))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(bookingService).getAllByOwnerIdAndState(1L, BookingRequestState.ALL);
    }
}
