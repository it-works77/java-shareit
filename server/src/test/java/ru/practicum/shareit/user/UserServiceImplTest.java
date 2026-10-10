package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.EntityAlreadyExistsException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void add_shouldSaveUserAndReturnDto() {
        UserCreateRequestDto createDto = new UserCreateRequestDto();
        createDto.setName("John");
        createDto.setEmail("john@example.com");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("John");
        savedUser.setEmail("john@example.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDto result = userService.add(createDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateById_whenAllFieldsProvided_shouldUpdateAndReturnDto() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old");
        existingUser.setEmail("old@example.com");

        UserUpdateRequestDto updateDto = new UserUpdateRequestDto();
        updateDto.setName("New");
        updateDto.setEmail("new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserResponseDto result = userService.updateById(1L, updateDto);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(existingUser.getName()).isEqualTo("New");
        assertThat(existingUser.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateById_whenOnlyNameProvided_shouldNotChangeEmail() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old");
        existingUser.setEmail("old@example.com");

        UserUpdateRequestDto updateDto = new UserUpdateRequestDto();
        updateDto.setName("New");
        updateDto.setEmail(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserResponseDto result = userService.updateById(1L, updateDto);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getEmail()).isEqualTo("old@example.com");
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateById_whenOnlyEmailProvided_shouldNotChangeName() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old");
        existingUser.setEmail("old@example.com");

        UserUpdateRequestDto updateDto = new UserUpdateRequestDto();
        updateDto.setName(null);
        updateDto.setEmail("new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserResponseDto result = userService.updateById(1L, updateDto);

        assertThat(result.getName()).isEqualTo("Old");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateById_whenAllFieldsNull_shouldKeepExistingData() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old");
        existingUser.setEmail("old@example.com");

        UserUpdateRequestDto updateDto = new UserUpdateRequestDto();
        updateDto.setName(null);
        updateDto.setEmail(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserResponseDto result = userService.updateById(1L, updateDto);

        assertThat(result.getName()).isEqualTo("Old");
        assertThat(result.getEmail()).isEqualTo("old@example.com");
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateById_whenUserNotFound_shouldThrowEntityNotFoundException() {
        UserUpdateRequestDto updateDto = new UserUpdateRequestDto();
        updateDto.setName("New");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateById(99L, updateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateById_whenEmailAlreadyExists_shouldThrowEntityAlreadyExistsException() {
        UserUpdateRequestDto updateDto = new UserUpdateRequestDto();
        updateDto.setName("New Name");
        updateDto.setEmail("taken@email.com");

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setName("Other");
        existingUser.setEmail("taken@email.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("taken@email.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.updateById(1L, updateDto))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessageContaining("taken@email.com");

        verify(userRepository).findByEmail("taken@email.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void add_whenEmailExists_shouldThrowEntityAlreadyExistsException() {
        UserCreateRequestDto request = new UserCreateRequestDto();
        request.setName("John");
        request.setEmail("john@example.com");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("John");
        existingUser.setEmail("john@example.com");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.add(request))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessageContaining("john@example.com");

        verify(userRepository).findByEmail("john@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void get_whenUserExists_shouldReturnDto() {
        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto result = userService.get(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void get_whenUserNotFound_shouldThrowEntityNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.get(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_shouldReturnListOfDtos() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("John");
        user1.setEmail("john@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Jane");
        user2.setEmail("jane@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponseDto> result = userService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("John");
        assertThat(result.get(1).getName()).isEqualTo("Jane");
    }

    @Test
    void remove_whenUserExists_shouldDeleteById() {
        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.remove(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void remove_whenUserNotFound_shouldThrowEntityNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.remove(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository, never()).deleteById(any());
    }
}
