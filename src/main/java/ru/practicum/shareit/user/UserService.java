package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateRequestDto;

import java.util.List;

public interface UserService {
    UserResponseDto add(UserCreateRequestDto userCreateRequestDto);

    UserResponseDto updateById(Long userId, UserUpdateRequestDto userUpdateRequestDto);

    UserResponseDto get(Long id);

    List<UserResponseDto> getAll();

    void remove(Long id);
}
