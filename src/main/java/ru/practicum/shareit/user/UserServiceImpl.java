package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Override
    public UserResponseDto add(UserRequestDto user) {
        return null;
    }

    @Override
    public UserResponseDto updateById(Long userId, UserRequestDto userRequestDto) {
        return null;
    }

    @Override
    public UserResponseDto get(Long id) {
        return null;
    }

    @Override
    public List<UserResponseDto> getAll() {
        return List.of();
    }

    @Override
    public boolean remove(Long id) {
        return false;
    }
}
