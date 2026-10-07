package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для создания нового бронирования.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateRequestDto {

    @NotNull
    @Positive
    private Long itemId;

    @NotNull
    @FutureOrPresent
    private LocalDateTime start;

    @NotNull
    @Future
    private LocalDateTime end;

    /**
     * ID пользователя, создающего бронирование.
     * Заполняется из заголовка static final String USER_ID_HEADER.
     */
    private Long bookerId;

    @AssertTrue(message = "Дата начала бронирования должна быть раньше даты окончания бронирования")
    public boolean isDatesValid() {
        return start.isBefore(end);
    }
}
