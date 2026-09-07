package ru.practicum.shareit.user;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;

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
    public UserResponseDto add(@RequestBody UserRequestDto userRequestDto) {
        return userService.add(userRequestDto);
    }

    @GetMapping("/{userId}")
    public  UserResponseDto get(@RequestParam @Positive Long userId) {
        return userService.get(userId);
    }

    @PatchMapping("/{userId}")
    public UserResponseDto update(@RequestParam Long userId,
                                  @RequestBody UserRequestDto userRequestDto) {
        userRequestDto.setId(userId);
        return userService.update(userRequestDto);
    }

    @DeleteMapping("/{userId}")
    public void delete(@RequestParam @Positive Long userId) {
        userService.remove(userId);
    }
}
