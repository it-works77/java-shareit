package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.enums.BookingRequestState;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BookingClient bookingClient;

    @Autowired
    MockMvc mockMvc;

    private BookingCreateRequestDto makeValidDto() {
        return BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();
    }

    @Test
    void add_shouldReturnCreatedBooking() throws Exception {
        BookingCreateRequestDto requestDto = makeValidDto();

        when(bookingClient.add(eq(1L), any(BookingCreateRequestDto.class)))
                .thenReturn(ResponseEntity.ok().body("{\"id\":10,\"status\":\"WAITING\"}"));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.status", is("WAITING")));

        verify(bookingClient).add(eq(1L), any(BookingCreateRequestDto.class));
    }

    @Test
    void add_whenItemIdIsNull_shouldReturnBadRequest() throws Exception {
        BookingCreateRequestDto requestDto = BookingCreateRequestDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).add(any(), any());
    }

    @Test
    void add_whenStartIsInPast_shouldReturnBadRequest() throws Exception {
        BookingCreateRequestDto requestDto = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).add(any(), any());
    }

    @Test
    void add_whenEndIsBeforeStart_shouldReturnBadRequest() throws Exception {
        BookingCreateRequestDto requestDto = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(3))
                .end(LocalDateTime.now().plusHours(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).add(any(), any());
    }

    @Test
    void add_whenUserIdHeaderIsMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(makeValidDto()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).add(any(), any());
    }

    @Test
    void add_whenUserIdIsNegative_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, -1L)
                        .content(objectMapper.writeValueAsString(makeValidDto()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).add(any(), any());
    }

    @Test
    void updateApprovement_shouldCallClientWithParams() throws Exception {
        when(bookingClient.updateApprovement(1L, 5L, true))
                .thenReturn(ResponseEntity.ok().body("{\"id\":5,\"status\":\"APPROVED\"}"));

        mockMvc.perform(patch("/bookings/{bookingId}", 5L)
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.status", is("APPROVED")));

        verify(bookingClient).updateApprovement(1L, 5L, true);
    }

    @Test
    void updateApprovement_whenApprovedParamIsMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/{bookingId}", 5L)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).updateApprovement(any(), any(), any());
    }

    @Test
    void getByUserIdAndBookingId_shouldReturnBooking() throws Exception {
        when(bookingClient.getByUserIdAndBookingId(1L, 5L))
                .thenReturn(ResponseEntity.ok().body("{\"id\":5,\"status\":\"WAITING\"}"));

        mockMvc.perform(get("/bookings/{bookingId}", 5L)
                        .header(USER_ID_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.status", is("WAITING")));

        verify(bookingClient).getByUserIdAndBookingId(1L, 5L);
    }

    @Test
    void getAllByBookerIdAndState_shouldUseDefaultStateAll() throws Exception {
        when(bookingClient.getAllByBookerIdAndState(eq(1L), eq(BookingRequestState.ALL)))
                .thenReturn(ResponseEntity.ok().body("[{\"id\":5}]"));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(5)));

        verify(bookingClient).getAllByBookerIdAndState(1L, BookingRequestState.ALL);
    }

    @Test
    void getAllByBookerIdAndState_withExplicitState_shouldPassState() throws Exception {
        when(bookingClient.getAllByBookerIdAndState(eq(1L), eq(BookingRequestState.WAITING)))
                .thenReturn(ResponseEntity.ok().body("[{\"id\":5}]"));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "WAITING")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingClient).getAllByBookerIdAndState(1L, BookingRequestState.WAITING);
    }

    @Test
    void getAllByBookerIdAndState_whenStateIsUnknown_shouldReturnError() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getAllByBookerIdAndState(any(), any());
    }

    @Test
    void getAllByOwnerIdAndState_shouldPassState() throws Exception {
        when(bookingClient.getAllByOwnerIdAndState(eq(1L), eq(BookingRequestState.FUTURE)))
                .thenReturn(ResponseEntity.ok().body("[{\"id\":7}]"));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "FUTURE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(7)));

        verify(bookingClient).getAllByOwnerIdAndState(1L, BookingRequestState.FUTURE);
    }

    @Test
    void getAllByOwnerIdAndState_whenHeaderIsMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getAllByOwnerIdAndState(any(), any());
    }
}
