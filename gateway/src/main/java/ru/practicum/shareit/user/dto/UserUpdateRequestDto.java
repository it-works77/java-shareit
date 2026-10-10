package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserUpdateRequestDto {
    private String name;

    @Email
    private String email;

    @JsonIgnore
    @AssertTrue(message = "Необходимо указать, как минимум, одно поле для обновления")
    public boolean isAtLeastOneFieldProvided() {
        return name != null || email != null;
    }
}
