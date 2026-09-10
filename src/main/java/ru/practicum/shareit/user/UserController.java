package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateRequestDto;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserResponseDto add(@Valid @RequestBody UserCreateRequestDto userCreateRequestDto) {
        return userService.add(userCreateRequestDto);
    }

    @GetMapping("/{userId}")
    public  UserResponseDto get(@PathVariable @Positive Long userId) {
        return userService.get(userId);
    }

    @PatchMapping("/{userId}")
    public UserResponseDto update(@PathVariable @Positive Long userId,
                                  @Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
        return userService.updateById(userId, userUpdateRequestDto);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable @Positive Long userId) {
        userService.remove(userId);
    }
}
