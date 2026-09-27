package ru.practicum.shareit.booking.mapper;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingMapper {
    public static Booking MapBookingRequestDtotoBooking(BookingCreateRequestDto bookingCreateRequestDto, Item item, User user) {
        return Booking.builder()
                .start(bookingCreateRequestDto.getStart())
                .end(bookingCreateRequestDto.getEnd())
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
    }

    public static BookingResponseDto MapBookingToBookingResponseDto(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(new BookingResponseDto.Booker(booking.getBooker().getId()))
                .item(new BookingResponseDto.Item(booking.getItem().getId(), booking.getItem().getName()))
                .build();
    }
}
