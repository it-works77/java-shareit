package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingRequestState;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto add(@RequestHeader(USER_ID_HEADER) Long bookerId,
                                  @RequestBody BookingCreateRequestDto bookingCreateRequestDto) {
        bookingCreateRequestDto.setBookerId(bookerId);
        return bookingService.create(bookingCreateRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateApprovement(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                                @PathVariable Long bookingId,
                                                @RequestParam(name = "approved") Boolean approved) {
        return bookingService.updateApprovement(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getByUserIdAndBookingId(@RequestHeader(USER_ID_HEADER) Long userId,
                                                      @PathVariable Long bookingId) {
        return bookingService.getByUserIdAndBookingId(userId, bookingId);
    }

    /**
     * Получение списка всех бронирований текущего пользователя.
     *
     * @param bookerId the ID of the booker (user) whose bookings should be returned
     * @param state    the booking state filter; defaults to {@code ALL}
     * @return a list of booking DTOs matching the booker and state
     */
    @GetMapping
    public List<BookingResponseDto> getAllByBookerIdAndState(@RequestHeader(USER_ID_HEADER) Long bookerId,
                                             @RequestParam(name = "state", defaultValue = "ALL") BookingRequestState state) {
        return bookingService.getAllByBookerIdAndState(bookerId, state);
    }

    /**
     * Получение списка бронирований для всех вещей текущего пользователя.
     *
     * @param ownerId the ID of the owner (user) whose bookings should be returned
     * @param state    the booking state filter; defaults to {@code ALL}
     * @return a list of booking DTOs matching the owner and state
     */
    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByOwnerIdAndState(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                            @RequestParam(name = "state", defaultValue = "ALL") BookingRequestState state) {
        return bookingService.getAllByOwnerIdAndState(ownerId, state);
    }
}
