package ru.practicum.shareit.booking;

import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.enums.BookingRequestState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingClientTest {
    @Mock
    private RestTemplate rest;

    @Mock
    private RestTemplateBuilder builder;

    private BookingClient client;

    @BeforeEach
    void setUp() {
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(rest);
        client = new BookingClient("http://localhost:9090", builder);
    }

    @Test
    void getAllByBookerIdAndState_sendsStateParamAndHeader() {
        when(rest.exchange(eq("?state={state}"), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(Object.class), eq(Map.of("state", "ALL"))))
                .thenReturn(ResponseEntity.ok("ok"));

        ResponseEntity<Object> response = client.getAllByBookerIdAndState(1L, BookingRequestState.ALL);

        assertThat(response.getBody()).isEqualTo("ok");
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq("?state={state}"), eq(HttpMethod.GET), captor.capture(),
                eq(Object.class), eq(Map.of("state", "ALL")));
        assertThat(captor.getValue().getHeaders().getFirst("X-Sharer-User-Id")).isEqualTo("1");
    }

    @Test
    void add_postsToRoot() {
        BookingCreateRequestDto dto = new BookingCreateRequestDto();
        when(rest.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("created"));

        client.add(2L, dto);

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq(""), eq(HttpMethod.POST), captor.capture(), eq(Object.class));
        assertThat(captor.getValue().getHeaders().getFirst("X-Sharer-User-Id")).isEqualTo("2");
        assertThat(captor.getValue().getBody()).isEqualTo(dto);
    }

    @Test
    void updateApprovement_expandsBookingIdAndApproved() {
        Map<String, Object> params = Map.of("bookingId", 5L, "approved", true);
        when(rest.exchange(eq("/{bookingId}?approved={approved}"), eq(HttpMethod.PATCH),
                any(HttpEntity.class), eq(Object.class), eq(params)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.updateApprovement(1L, 5L, true);

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq("/{bookingId}?approved={approved}"), eq(HttpMethod.PATCH),
                captor.capture(), eq(Object.class), eq(params));
        assertThat(captor.getValue().getHeaders().getFirst("X-Sharer-User-Id")).isEqualTo("1");
    }

    @Test
    void getByUserIdAndBookingId_usesPathId() {
        when(rest.exchange(eq("/5"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.getByUserIdAndBookingId(1L, 5L);

        verify(rest).exchange(eq("/5"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void getAllByOwnerIdAndState_sendsOwnerPath() {
        when(rest.exchange(eq("/owner?state={state}"), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(Object.class), eq(Map.of("state", "WAITING"))))
                .thenReturn(ResponseEntity.ok("ok"));

        client.getAllByOwnerIdAndState(3L, BookingRequestState.WAITING);

        verify(rest).exchange(eq("/owner?state={state}"), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(Object.class), eq(Map.of("state", "WAITING")));
    }

}
