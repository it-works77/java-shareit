package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {
    @Positive
    private Long id;

    private String name;

    @Email
    private String email;
}
