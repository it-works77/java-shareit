package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingRequestState;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(BookingCreateRequestDto bookingCreateRequestDto);

    BookingResponseDto updateApprovement(Long bookerId, Long bookingId, Boolean approved);

    BookingResponseDto getByUserIdAndBookingId(Long userId, Long bookingId);

    List<BookingResponseDto> getAllByBookerIdAndState(Long bookerId, BookingRequestState state);

    List<BookingResponseDto> getAllByOwnerIdAndState(Long ownerId, BookingRequestState state);
}
