package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.UserUpdateRequestDto;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> add(@Valid @RequestBody UserCreateRequestDto userCreateRequestDto) {
        log.info("Adding user {}", userCreateRequestDto);
        return userClient.add(userCreateRequestDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> get(@PathVariable @Positive Long userId) {
        log.info("Getting user with id {}", userId);
        return userClient.getByUserId(userId);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@PathVariable @Positive Long userId,
                                         @Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
        log.info("Updating user with id {}", userId);
        return userClient.updateById(userId, userUpdateRequestDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(@PathVariable @Positive Long userId) {
        log.info("Deleting user with id {}", userId);
        return userClient.deleteById(userId);
    }
}
