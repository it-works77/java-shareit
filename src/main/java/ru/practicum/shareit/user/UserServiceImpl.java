package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateRequestDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserResponseDto add(UserCreateRequestDto userCreateRequestDto) {
        log.info("Add user: userDto={}", userCreateRequestDto);
        User user = userRepository.add(UserMapper.mapUserCreateRequestDtoToUser(userCreateRequestDto));
        return UserMapper.mapUserToUserResponseDto(user);
    }

    @Override
    public UserResponseDto updateById(Long userId, UserUpdateRequestDto userUpdateRequestDto) {
        log.info("Update user: userDto={}", userUpdateRequestDto);

        if (userId == null) {
            throw new IllegalArgumentException("userId должен быть задан");
        }

        User existingUser = userRepository.get(userId).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден по id=%s".formatted(userId)));

        User newUser = UserMapper.mapUserUpdateRequestDtoToUser(userUpdateRequestDto);
        newUser.setId(userId);

        User updatedUser = userRepository.update(newUser);
        return UserMapper.mapUserToUserResponseDto(updatedUser);
    }

    @Override
    public UserResponseDto get(Long id) {
        log.info("Get user: userId={}", id);
        User user = userRepository.get(id).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден по id=%s".formatted(id)));
        return UserMapper.mapUserToUserResponseDto(user);
    }

    @Override
    public List<UserResponseDto> getAll() {
        log.info("Get all users");
        List<User> users = userRepository.getAll();
        return users.stream()
                .map(UserMapper::mapUserToUserResponseDto)
                .toList();
    }

    @Override
    public void remove(Long id) {
        log.info("Remove user: userId={}", id);
        if (userRepository.remove(id)) {
            log.info("User deleted. UserId={}", id);
        } else {
            throw new EntityNotFoundException("Пользователь не найден по id=%s".formatted(id));
        }
    }
}
