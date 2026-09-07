package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder
public class ItemResponseDto {
    Long id;
    String name;
    String description;
    boolean available;
}
