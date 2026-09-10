package ru.practicum.shareit.user.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.dto.UserUpdateRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {
    public static User mapUserCreateRequestDtoToUser(UserCreateRequestDto userCreateRequestDto) {
        return User.builder()
                .name(userCreateRequestDto.getName())
                .email(userCreateRequestDto.getEmail())
                .build();
    }

    public static User mapUserUpdateRequestDtoToUser(UserUpdateRequestDto userUpdateRequestDto) {
        return User.builder()
                .name(userUpdateRequestDto.getName())
                .email(userUpdateRequestDto.getEmail())
                .build();
    }

    public static UserResponseDto mapUserToUserResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
