package ru.practicum.shareit.user;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EntityAlreadyExistsException;
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
    @Transactional
    public UserResponseDto add(UserCreateRequestDto userCreateRequestDto) {
        log.info("Add user: userDto={}", userCreateRequestDto);
        userRepository.findByEmail(userCreateRequestDto.getEmail()).ifPresent(user -> {
            throw new EntityAlreadyExistsException("Пользователь с email=%s уже существует".formatted(user.getEmail()));
        });

        User user = userRepository.save(UserMapper.mapUserCreateRequestDtoToUser(userCreateRequestDto));
        return UserMapper.mapUserToUserResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateById(Long userId, UserUpdateRequestDto userUpdateRequestDto) {
        log.info("Update user: userDto={}", userUpdateRequestDto);

        if (userId == null) {
            throw new IllegalArgumentException("userId должен быть задан");
        }

        User existingUser = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден по id=%s".formatted(userId)));

        userRepository.findByEmail(userUpdateRequestDto.getEmail()).ifPresent(user -> {
            throw new EntityAlreadyExistsException("Пользователь с email=%s уже существует".formatted(user.getEmail()));
        });

        // Обновляем только не null поля
        if (userUpdateRequestDto.getName() != null) {
            existingUser.setName(userUpdateRequestDto.getName());
        }
        if (userUpdateRequestDto.getEmail() != null) {
            existingUser.setEmail(userUpdateRequestDto.getEmail());
        }

        User updatedUser = userRepository.save(existingUser);
        return UserMapper.mapUserToUserResponseDto(updatedUser);
    }

    @Override
    public UserResponseDto get(Long id) {
        log.info("Get user: userId={}", id);
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден по id=%s".formatted(id)));
        return UserMapper.mapUserToUserResponseDto(user);
    }

    @Override
    public List<UserResponseDto> getAll() {
        log.info("Get all users");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserMapper::mapUserToUserResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public void remove(Long id) {
        log.info("Remove user: userId={}", id);
        userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден по id=%s".formatted(id)));

        userRepository.deleteById(id);
        log.info("User deleted. UserId={}", id);

    }
}
