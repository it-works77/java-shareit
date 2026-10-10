package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.time.LocalDateTime;

/**
 * DTO для ответа с информацией о бронировании.
 */
@Data
@Builder
public class BookingResponseDto {

    private Long id;

    private LocalDateTime start;

    private LocalDateTime end;

    private BookingStatus status;

    private Booker booker;

    private Item item;

    public record Booker(Long id) {
    }

    public record Item(Long id, String name) {
    }
}

