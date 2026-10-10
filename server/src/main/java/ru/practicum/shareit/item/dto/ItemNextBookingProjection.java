package ru.practicum.shareit.item.dto;

import java.time.LocalDateTime;

public interface ItemNextBookingProjection {
    Long getId();

    LocalDateTime getNextBooking();
}
