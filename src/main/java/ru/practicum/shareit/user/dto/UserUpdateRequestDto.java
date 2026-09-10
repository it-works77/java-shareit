package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequestDto {
    String name;

    @Email
    String email;

    @AssertTrue(message = "Необходимо указать, как минимум, одно поле для обновления")
    public boolean isAtLeastOneFieldProvided() {
        return name != null || email != null;
    }
}
