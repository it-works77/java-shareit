package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.shareit.user.dto.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.UserUpdateRequestDto;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserClient userClient;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
    }

    // add
    @Test
    void add_whenRequestIsValid_shouldReturnOk() throws Exception {
        UserCreateRequestDto userCreateRequestDto = createValidDto("name", "email@email.com");
        when(userClient.add(any(UserCreateRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequestDto)))
                .andExpect(status().isOk());

        verify(userClient).add(userCreateRequestDto);
    }

    @Test
    void add_whenNotValidEmail_shouldReturnBadRequest() throws Exception {
        UserCreateRequestDto userCreateRequestDto = createValidDto("name", "email");
        when(userClient.add(any(UserCreateRequestDto.class)))
                .thenReturn(ResponseEntity.badRequest().build());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).add(userCreateRequestDto);
    }

    @Test
    void add_whenNotNameEmail_shouldReturnBadRequest() throws Exception {
        UserCreateRequestDto userCreateRequestDto = createValidDto("", "email");
        when(userClient.add(any(UserCreateRequestDto.class)))
                .thenReturn(ResponseEntity.badRequest().build());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).add(userCreateRequestDto);
    }

    @Test
    void add_whenNoEmail_shouldReturnBadRequest() throws Exception {
        UserCreateRequestDto userCreateRequestDto = createValidDto("name", null);
        when(userClient.add(any(UserCreateRequestDto.class)))
                .thenReturn(ResponseEntity.badRequest().build());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).add(userCreateRequestDto);
    }

    @Test
    void add_whenNoName_shouldReturnBadRequest() throws Exception {
        UserCreateRequestDto userCreateRequestDto = createValidDto(null, "email@email.com");
        when(userClient.add(any(UserCreateRequestDto.class)))
                .thenReturn(ResponseEntity.badRequest().build());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateRequestDto)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).add(userCreateRequestDto);
    }

    @Test
    void add_whenMalformedJson_shouldReturnBadRequest() throws Exception {
        String malformedJson = "{ \"name\": \"test\", \"email\": \"test@test.com\"";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    // get
    @Test
    void get_whenRequestIsValid_shouldReturnOk() throws Exception {
        Long userId = 1L;
        when(userClient.getByUserId(anyLong())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk());
        verify(userClient).getByUserId(userId);
    }

    @Test
    void get_whenUserIdNotValid_shouldReturnBadRequest() throws Exception {
        Long userId = 0L;
        when(userClient.getByUserId(anyLong())).thenReturn(ResponseEntity.badRequest().build());

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isBadRequest());
        verify(userClient, never()).getByUserId(userId);
    }

    @Test
    void get_whenUserIdNegative_shouldReturnBadRequest() throws Exception {
        Long userId = -1L;

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isBadRequest());
        verify(userClient, never()).getByUserId(userId);
    }

    // update
    @Test
    void update_whenRequestIsValid_shouldReturnOk() throws Exception {
        Long userId = 1L;
        UserUpdateRequestDto updateDto = createUpdateDto("name", "new@email.com");

        when(userClient.updateById(anyLong(), any(UserUpdateRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        verify(userClient).updateById(userId, updateDto);
    }

    @Test
    void update_whenOnlyNameProvided_shouldReturnOk() throws Exception {
        Long userId = 1L;
        UserUpdateRequestDto updateDto = createUpdateDto(null, "new@email.com");

        when(userClient.updateById(anyLong(), any(UserUpdateRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        verify(userClient).updateById(userId, updateDto);
    }

    @Test
    void update_whenOnlyEmailProvided_shouldReturnOk() throws Exception {
        Long userId = 1L;
        UserUpdateRequestDto updateDto = createUpdateDto(null, "new@email.com");

        when(userClient.updateById(anyLong(), any(UserUpdateRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        verify(userClient).updateById(userId, updateDto);
    }

    @Test
    void update_whenUserIdNotValid_shouldReturnBadRequest() throws Exception {
        Long userId = 0L;
        UserUpdateRequestDto updateDto = createUpdateDto("newName", null);

        when(userClient.updateById(anyLong(), any(UserUpdateRequestDto.class)))
                .thenReturn(ResponseEntity.badRequest().build());

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
        verify(userClient, never()).updateById(anyLong(), any());
    }

    @Test
    void update_whenUserIdNegative_shouldReturnBadRequest() throws Exception {
        Long userId = -1L;
        UserUpdateRequestDto updateDto = createUpdateDto(null, null);
        updateDto.setName("newName");

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
        verify(userClient, never()).updateById(anyLong(), any());
    }

    @Test
    void update_whenEmptyBody_shouldReturnBadRequest() throws Exception {
        Long userId = 1L;
        UserUpdateRequestDto updateDto = createUpdateDto(null, null);

        when(userClient.updateById(anyLong(), any(UserUpdateRequestDto.class)))
                .thenReturn(ResponseEntity.badRequest().build());

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
        verify(userClient, never()).updateById(anyLong(), any());
    }

    @Test
    void update_whenInvalidEmail_shouldReturnBadRequest() throws Exception {
        Long userId = 1L;
        UserUpdateRequestDto updateDto = createUpdateDto(null, null);
        updateDto.setEmail("invalid-email");

        when(userClient.updateById(anyLong(), any(UserUpdateRequestDto.class)))
                .thenReturn(ResponseEntity.badRequest().build());

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
        verify(userClient, never()).updateById(anyLong(), any());
    }

    @Test
    void update_whenMalformedJson_shouldReturnBadRequest() throws Exception {
        Long userId = 1L;
        String malformedJson = "{ \"name\": \"test\"";

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    // delete
    @Test
    void delete_whenRequestIsValid_shouldReturnOk() throws Exception {
        Long userId = 1L;
        when(userClient.deleteById(anyLong())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userClient).deleteById(userId);
    }

    @Test
    void delete_whenUserIdNotValid_shouldReturnBadRequest() throws Exception {
        Long userId = 0L;
        when(userClient.deleteById(anyLong())).thenReturn(ResponseEntity.badRequest().build());

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isBadRequest());
        verify(userClient, never()).deleteById(anyLong());
    }

    @Test
    void delete_whenUserIdNegative_shouldReturnBadRequest() throws Exception {
        Long userId = -1L;

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isBadRequest());
        verify(userClient, never()).deleteById(anyLong());
    }

    private UserCreateRequestDto createValidDto(String name, String email) {
        UserCreateRequestDto dto = new UserCreateRequestDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    private static UserUpdateRequestDto createUpdateDto(String name, String email) {
        UserUpdateRequestDto updateDto = new UserUpdateRequestDto();
        updateDto.setName(name);
        updateDto.setEmail(email);
        return updateDto;
    }

}