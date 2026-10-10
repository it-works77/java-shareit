package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sprint add-item-requests.
 */
@Data
@NoArgsConstructor
public class ItemRequestRequestDto {
    @NotBlank(message = "Описание не может быть пустым")
    private String description;
}
