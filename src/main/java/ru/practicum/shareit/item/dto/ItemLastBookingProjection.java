package ru.practicum.shareit.item.dto;

import java.time.LocalDateTime;

public interface ItemLastBookingProjection {
    Long getId();
    LocalDateTime getLastBooking();
}
