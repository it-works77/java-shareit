package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.enums.BookingRequestState;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(USER_ID_HEADER) @Positive long userId,
                                      @RequestBody @Valid BookingCreateRequestDto requestDto) {
        log.info("Creating booking {}, userId={}", requestDto, userId);
        return bookingClient.add(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateApprovement(@RequestHeader(USER_ID_HEADER) @Positive Long ownerId,
                                                    @PathVariable @Positive Long bookingId,
                                                    @RequestParam(name = "approved") Boolean approved) {
        log.info("Updating booking {}, ownerId={}, approved={}", bookingId, ownerId, approved);
        return bookingClient.updateApprovement(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getByUserIdAndBookingId(@RequestHeader(USER_ID_HEADER) @Positive long userId,
                                                          @PathVariable @Positive Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getByUserIdAndBookingId(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByBookerIdAndState(@RequestHeader(USER_ID_HEADER) @Positive long userId,
                                                           @RequestParam(name = "state", defaultValue = "ALL") String stateParam) {
        BookingRequestState state = BookingRequestState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Get booking with state {}, bookerId={}", stateParam, userId);
        return bookingClient.getAllByBookerIdAndState(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwnerIdAndState(@RequestHeader(USER_ID_HEADER) @Positive Long ownerId,
                                                          @RequestParam(name = "state", defaultValue = "ALL") String stateParam) {
        BookingRequestState state = BookingRequestState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Get booking with state {}, ownerId={}", stateParam, ownerId);
        return bookingClient.getAllByOwnerIdAndState(ownerId, state);
    }
}
