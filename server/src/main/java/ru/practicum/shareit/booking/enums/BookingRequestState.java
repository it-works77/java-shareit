package ru.practicum.shareit.booking.enums;

import lombok.Getter;

@Getter
public enum BookingRequestState {
        ALL,
        CURRENT,
        PAST,
        FUTURE,
        WAITING,
        REJECTED

}
