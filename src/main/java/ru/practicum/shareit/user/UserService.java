package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.util.List;

public interface UserService {
    UserResponseDto add(UserRequestDto userRequestDto);

    UserResponseDto updateById(Long userId, UserRequestDto userRequestDto);

    UserResponseDto get(Long id);

    List<UserResponseDto> getAll();

    boolean remove(Long id);
}
