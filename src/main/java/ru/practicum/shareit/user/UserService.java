package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserResponseDto add(UserRequestDto userRequestDto);

    UserResponseDto update(UserRequestDto user);

    UserResponseDto get(Long id);

    List<UserResponseDto> getAll();

    boolean remove(Long id);
}
