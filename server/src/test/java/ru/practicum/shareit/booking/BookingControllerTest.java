package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
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

    private BookingCreateRequestDto validCreateDto;
    private BookingResponseDto responseDto;

    @BeforeEach
    void setUp() {
        validCreateDto = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        responseDto = BookingResponseDto.builder()
                .id(1L)
                .start(validCreateDto.getStart())
                .end(validCreateDto.getEnd())
                .build();
    }

    // POST /bookings - Add booking
    @Test
    void addBooking_shouldSetBookerIdFromHeaderAndReturnCreatedBooking() throws Exception {
        when(bookingService.create(any(BookingCreateRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        ArgumentCaptor<BookingCreateRequestDto> captor = ArgumentCaptor.forClass(BookingCreateRequestDto.class);
        verify(bookingService).create(captor.capture());
        assertThat(captor.getValue().getBookerId()).isEqualTo(1L);
    }

    @Test
    void addBooking_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void addBooking_withInvalidUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void addBooking_withZeroUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void addBooking_withNegativeUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, -1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void addBooking_withMissingItemId_shouldReturnBadRequest() throws Exception {
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void addBooking_withZeroItemId_shouldReturnBadRequest() throws Exception {
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(0L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void addBooking_withNegativeItemId_shouldReturnBadRequest() throws Exception {
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(-1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void addBooking_withMissingStart_shouldReturnBadRequest() throws Exception {
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(1L)
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void addBooking_withMissingEnd_shouldReturnBadRequest() throws Exception {
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void addBooking_withStartInPast_shouldReturnBadRequest() throws Exception {
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void addBooking_withEndNotFuture_shouldReturnBadRequest() throws Exception {
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void addBooking_withStartAfterEnd_shouldReturnBadRequest() throws Exception {
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void addBooking_withMalformedJson_shouldReturnBadRequest() throws Exception {
        String malformedJson = "{\"itemId\":1,\"start\":\"2025-01-01T10:00:00\"";

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    // PATCH /bookings/{bookingId} - Update approvement
    @Test
    void updateApprovement_shouldPassOwnerAndBookingAndApproved() throws Exception {
        when(bookingService.updateApprovement(1L, 1L, true)).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(bookingService).updateApprovement(1L, 1L, true);
    }

    @Test
    void updateApprovement_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void updateApprovement_withInvalidUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, "abc")
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void updateApprovement_withZeroUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 0)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void updateApprovement_withNegativeUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, -1)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void updateApprovement_withInvalidBookingId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/abc")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void updateApprovement_withZeroBookingId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/0")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void updateApprovement_withNegativeBookingId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/-1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void updateApprovement_withoutApprovedParam_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void updateApprovement_withInvalidApprovedParam_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "invalid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void updateApprovement_withApprovedFalse_shouldReturnOk() throws Exception {
        when(bookingService.updateApprovement(1L, 1L, false)).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(bookingService).updateApprovement(1L, 1L, false);
    }

    // GET /bookings/{bookingId} - Get by user and booking ID
    @Test
    void getByUserIdAndBookingId_shouldReturnBooking() throws Exception {
        when(bookingService.getByUserIdAndBookingId(1L, 1L)).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(bookingService).getByUserIdAndBookingId(1L, 1L);
    }

    @Test
    void getByUserIdAndBookingId_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getByUserIdAndBookingId_withInvalidUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getByUserIdAndBookingId_withZeroUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, 0))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getByUserIdAndBookingId_withNegativeUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, -1))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getByUserIdAndBookingId_withInvalidBookingId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/abc")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getByUserIdAndBookingId_withZeroBookingId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/0")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getByUserIdAndBookingId_withNegativeBookingId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/-1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    // GET /bookings - Get all by booker ID and state
    @Test
    void getAllByBookerIdAndState_shouldReturnListOfBookings() throws Exception {
        when(bookingService.getAllByBookerIdAndState(1L, BookingRequestState.ALL))
                .thenReturn(List.of(responseDto));

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
    void getAllByBookerIdAndState_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getAllByBookerIdAndState_withInvalidUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, "abc")
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getAllByBookerIdAndState_withZeroUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 0)
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getAllByBookerIdAndState_withNegativeUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, -1)
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getAllByBookerIdAndState_withInvalidState_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getAllByBookerIdAndState_withAllStates_shouldReturnOk() throws Exception {
        for (BookingRequestState state : BookingRequestState.values()) {
            when(bookingService.getAllByBookerIdAndState(1L, state)).thenReturn(List.of());

            mockMvc.perform(get("/bookings")
                            .header(USER_ID_HEADER, 1L)
                            .param("state", state.name()))
                    .andExpect(status().isOk());

            verify(bookingService).getAllByBookerIdAndState(1L, state);
        }
    }

    // GET /bookings/owner - Get all by owner ID and state
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

    @Test
    void getAllByOwnerIdAndState_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getAllByOwnerIdAndState_withInvalidUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, "abc")
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getAllByOwnerIdAndState_withZeroUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 0)
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getAllByOwnerIdAndState_withNegativeUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, -1)
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getAllByOwnerIdAndState_withInvalidState_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getAllByOwnerIdAndState_withAllStates_shouldReturnOk() throws Exception {
        for (BookingRequestState state : BookingRequestState.values()) {
            when(bookingService.getAllByOwnerIdAndState(1L, state)).thenReturn(List.of());

            mockMvc.perform(get("/bookings/owner")
                            .header(USER_ID_HEADER, 1L)
                            .param("state", state.name()))
                    .andExpect(status().isOk());

            verify(bookingService).getAllByOwnerIdAndState(1L, state);
        }
    }
}