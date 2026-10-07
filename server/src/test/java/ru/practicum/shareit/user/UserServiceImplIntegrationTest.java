package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityAlreadyExistsException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateRequestDto;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplIntegrationTest {
    private final UserServiceImpl userService;

    @Test
    void shouldHandleUserLifecycle() {
        var name = "John";
        var email = "john@example.com";

        var newName = "Jane";
        var newEmail = "jane@example.com";

        var existingEmail = "existing@example.com";

        // Создали
        UserResponseDto existingUser = saveUser(name, existingEmail);

        UserResponseDto user = saveUser(name, email);
        UserResponseDto createdUser = userService.get(user.getId());
        assertEquals(user, createdUser);

        // Обновили все
        UserUpdateRequestDto userUpdateRequestDto1 = new UserUpdateRequestDto();
        userUpdateRequestDto1.setName(newName);
        userUpdateRequestDto1.setEmail(newEmail);

        UserResponseDto updatedUser = userService.updateById(user.getId(), userUpdateRequestDto1);
        assertEquals(newName, updatedUser.getName());
        assertEquals(newEmail, updatedUser.getEmail());

        // Нельзя создать с тем же email
        assertThrows(EntityAlreadyExistsException.class, () -> saveUser(newName, existingEmail));

        // Нельзя обновить с тем же email
        UserUpdateRequestDto userUpdateRequestDto2 = new UserUpdateRequestDto();
        userUpdateRequestDto2.setEmail(existingEmail);
        assertThrows(EntityAlreadyExistsException.class,
                () -> userService.updateById(user.getId(), userUpdateRequestDto2));

        // Удалили
        userService.remove(user.getId());
        assertThrows(EntityNotFoundException.class, () -> userService.get(user.getId()));

    }

    private UserResponseDto saveUser(String name, String email) {
        UserCreateRequestDto userCreateRequestDto = new UserCreateRequestDto();
        userCreateRequestDto.setName(name);
        userCreateRequestDto.setEmail(email);
        return userService.add(userCreateRequestDto);
    }
}