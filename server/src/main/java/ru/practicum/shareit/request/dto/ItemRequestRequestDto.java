package ru.practicum.shareit.request.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sprint add-item-requests.
 */
@Data
@NoArgsConstructor
public class ItemRequestRequestDto {
    private String description;
}
