package ru.practicum.shareit.request;

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

import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestClientTest {
    @Mock
    private RestTemplate rest;

    @Mock
    private RestTemplateBuilder builder;

    private ItemRequestClient client;

    @BeforeEach
    void setUp() {
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(rest);
        client = new ItemRequestClient("http://localhost:9090", builder);
    }

    @Test
    void addByUserId_postsToRoot() {
        ItemRequestRequestDto dto = new ItemRequestRequestDto();
        when(rest.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.addByUserId(1L, dto);

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq(""), eq(HttpMethod.POST), captor.capture(), eq(Object.class));
        assertThat(captor.getValue().getBody()).isEqualTo(dto);
        assertThat(captor.getValue().getHeaders().getFirst("X-Sharer-User-Id")).isEqualTo("1");
    }

    @Test
    void getById_usesPathId() {
        when(rest.exchange(eq("/5"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.getById(1L, 5L);

        verify(rest).exchange(eq("/5"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void getAllByRequestorId_usesRootPath() {
        when(rest.exchange(eq(""), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.getAllByRequestorId(1L);

        verify(rest).exchange(eq(""), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void getAllNotMy_usesAllPath() {
        when(rest.exchange(eq("/all"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.getAllNotMy(1L);

        verify(rest).exchange(eq("/all"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
    }
}
