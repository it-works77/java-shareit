package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */

@Data
public class ItemRequestDto {
    String name;
    String description;
    Boolean available;

    @AssertTrue(message = "Необходимо указать, как минимум, одно поле для обновления")
    public boolean isAtLeastOneFieldProvided() {
        return name != null || description != null || available != null;
    }
}
