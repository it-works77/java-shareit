package ru.practicum.shareit.item.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO Sprint add-controllers.
 */

@Data
@NoArgsConstructor
public class ItemCreateRequestDto {
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}
