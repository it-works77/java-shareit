package ru.practicum.shareit.user;

import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import ru.practicum.shareit.user.dto.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.UserUpdateRequestDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserClientTest {
    @Mock
    private RestTemplate rest;

    @Mock
    private RestTemplateBuilder builder;

    private UserClient client;

    @BeforeEach
    void setUp() {
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(rest);
        client = new UserClient("http://localhost:9090", builder);
    }

    @Test
    void add_postsWithoutUserHeader() {
        UserCreateRequestDto dto = new UserCreateRequestDto();
        when(rest.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.add(dto);

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq(""), eq(HttpMethod.POST), captor.capture(), eq(Object.class));
        assertThat(captor.getValue().getBody()).isEqualTo(dto);
        assertThat(captor.getValue().getHeaders().getFirst("X-Sharer-User-Id")).isNull();
    }

    @Test
    void getByUserId_usesPathId() {
        when(rest.exchange(eq("/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.getByUserId(1L);

        verify(rest).exchange(eq("/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void updateById_patchesById() {
        UserUpdateRequestDto dto = new UserUpdateRequestDto();
        when(rest.exchange(eq("/1"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.updateById(1L, dto);

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq("/1"), eq(HttpMethod.PATCH), captor.capture(), eq(Object.class));
        assertThat(captor.getValue().getBody()).isEqualTo(dto);
    }

    @Test
    void deleteById_deletesById() {
        when(rest.exchange(eq("/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        client.deleteById(1L);

        verify(rest).exchange(eq("/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class));
    }
}
