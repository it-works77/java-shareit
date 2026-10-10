package ru.practicum.shareit.booking;

import java.util.Map;

import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.enums.BookingRequestState;
import ru.practicum.shareit.client.BaseClient;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getAllByBookerIdAndState(long userId, BookingRequestState state) {
        Map<String, Object> parameters = Map.of(
                "state", state.name()
        );
        return get("?state={state}", userId, parameters);
    }


    public ResponseEntity<Object> add(long userId, BookingCreateRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> updateApprovement(Long ownerId, Long bookingId, Boolean approved) {
        return patch("/{bookingId}?approved={approved}", ownerId,
                Map.of("bookingId", bookingId, "approved", approved), null);
    }

    public ResponseEntity<Object> getByUserIdAndBookingId(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllByOwnerIdAndState(@Positive Long ownerId, BookingRequestState state) {
        return get("/owner?state={state}", ownerId, Map.of("state", state.name()));
    }
}
