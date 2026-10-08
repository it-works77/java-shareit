package ru.practicum.shareit.item;

import java.util.Map;
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

import ru.practicum.shareit.item.dto.CommentCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemUpdateRequestDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemClientTest {
    @Mock
    private RestTemplate rest;

    @Mock
    private RestTemplateBuilder builder;

    private ItemClient client;

    @BeforeEach
    void setUp() {
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(rest);
        client = new ItemClient("http://localhost:9090", builder);
    }

    @Test
    void add_postsToRoot() {
        ItemCreateRequestDto dto = new ItemCreateRequestDto();
        when(rest.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.add(1L, dto);

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq(""), eq(HttpMethod.POST), captor.capture(), eq(Object.class));
        assertThat(captor.getValue().getBody()).isEqualTo(dto);
        assertThat(captor.getValue().getHeaders().getFirst("X-Sharer-User-Id")).isEqualTo("1");
    }

    @Test
    void getById_usesPathId() {
        when(rest.exchange(eq("/7"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.getById(1L, 7L);

        verify(rest).exchange(eq("/7"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void getAllByOwnerId_usesRootPath() {
        when(rest.exchange(eq(""), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.getAllByOwnerId(1L);

        verify(rest).exchange(eq(""), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void search_appendsTextToPath() {
        when(rest.exchange(eq("/search?text=drill"), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.search("drill");

        verify(rest).exchange(eq("/search?text=drill"), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(Object.class));
    }

    @Test
    void updateById_patchesById() {
        ItemUpdateRequestDto dto = new ItemUpdateRequestDto();
        when(rest.exchange(eq("/7"), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.updateById(1L, 7L, dto);

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq("/7"), eq(HttpMethod.PATCH), captor.capture(), eq(Object.class));
        assertThat(captor.getValue().getBody()).isEqualTo(dto);
    }

    @Test
    void remove_deletesByIdWithoutHeader() {
        when(rest.exchange(eq("/7"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        client.remove(7L);

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq("/7"), eq(HttpMethod.DELETE), captor.capture(), eq(Object.class));
        assertThat(captor.getValue().getHeaders().getFirst("X-Sharer-User-Id")).isNull();
    }

    @Test
    void addComment_expandsItemId() {
        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        Map<String, Object> params = Map.of("itemId", 7L);
        when(rest.exchange(eq("/{itemId}/comment"), eq(HttpMethod.POST), any(HttpEntity.class),
                eq(Object.class), eq(params)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.addComment(1L, 7L, dto);

        verify(rest).exchange(eq("/{itemId}/comment"), eq(HttpMethod.POST), any(HttpEntity.class),
                eq(Object.class), eq(params));
    }
}
