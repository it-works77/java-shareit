package ru.practicum.shareit.client;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseClientTest {
    @Mock
    private RestTemplate rest;

    private BaseClient client;

    @BeforeEach
    void setUp() {
        client = new BaseClient(rest);
    }

    @Test
    void getWithoutUserId_noUserHeader() {
        when(rest.exchange(eq("/items"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        ResponseEntity<Object> response = client.get("/items");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(headerOfExchange()).doesNotContainKey("X-Sharer-User-Id");
    }

    @Test
    void getWithUserId_setsUserHeader() {
        when(rest.exchange(eq("/items"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.get("/items", 1L);

        assertThat(headerOfExchange().getFirst("X-Sharer-User-Id")).isEqualTo("1");
    }

    @Test
    void getWithParams_passesParamsMap() {
        Map<String, Object> params = Map.of("state", "ALL");
        when(rest.exchange(eq("?state={state}"), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(Object.class), eq(params)))
                .thenReturn(ResponseEntity.ok(List.of()));

        client.get("?state={state}", 1L, params);

        verify(rest).exchange(eq("?state={state}"), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(Object.class), eq(params));
    }

    @Test
    void postWithUserIdAndBody_sendsBodyAndHeader() {
        Object body = Map.of("name", "Drill");
        when(rest.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("created"));

        ResponseEntity<Object> response = client.post("", 2L, body);

        assertThat(response.getBody()).isEqualTo("created");
        ArgumentCaptor<HttpEntity<?>> captor = entityCaptor(HttpMethod.POST, "");
        assertThat(captor.getValue().getHeaders().getFirst("X-Sharer-User-Id")).isEqualTo("2");
        assertThat(captor.getValue().getBody()).isEqualTo(body);
    }

    @Test
    void postWithParams_passesParamsMap() {
        Object body = Map.of("itemId", 1);
        Map<String, Object> params = Map.of("itemId", 1);
        when(rest.exchange(eq("/{itemId}/comment"), eq(HttpMethod.POST), any(HttpEntity.class),
                eq(Object.class), eq(params)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.post("/{itemId}/comment", 1L, params, body);

        verify(rest).exchange(eq("/{itemId}/comment"), eq(HttpMethod.POST), any(HttpEntity.class),
                eq(Object.class), eq(params));
    }

    @Test
    void postWithoutUserId_noUserHeader() {
        when(rest.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.post("", Map.of("name", "John"));

        assertThat(headerOfExchange()).doesNotContainKey("X-Sharer-User-Id");
    }

    @Test
    void put_sendsPut() {
        Object body = Map.of("name", "x");
        when(rest.exchange(eq("/1"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.put("/1", 1L, body);

        verify(rest).exchange(eq("/1"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void patchOverloads_allReachExchange() {
        when(rest.exchange(any(String.class), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok("ok"));
        when(rest.exchange(any(String.class), eq(HttpMethod.PATCH), any(HttpEntity.class),
                eq(Object.class), any(Map.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        client.patch("/1", Map.of("a", 1));
        client.patch("/1", 1L);
        client.patch("/1", 1L, Map.of("a", 1));
        client.patch("/1", 1L, Map.of("a", 1), null);

        verify(rest, org.mockito.Mockito.times(3)).exchange(eq("/1"), eq(HttpMethod.PATCH),
                any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void deleteOverloads_allReachExchange() {
        when(rest.exchange(any(String.class), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());
        when(rest.exchange(any(String.class), eq(HttpMethod.DELETE), any(HttpEntity.class),
                eq(Object.class), any(Map.class)))
                .thenReturn(ResponseEntity.ok().build());

        client.delete("/1");
        client.delete("/1", 1L);
        client.delete("/1", 1L, Map.of("x", 1));

        verify(rest, org.mockito.Mockito.times(2)).exchange(eq("/1"), eq(HttpMethod.DELETE),
                any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void exchangeThrowsHttpStatus_returnsStatusAndBody() {
        HttpStatusCodeException ex = mock(HttpStatusCodeException.class);
        when(ex.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(ex.getResponseBodyAsByteArray()).thenReturn("{\"e\":1}".getBytes(StandardCharsets.UTF_8));
        when(rest.exchange(eq("/x"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(ex);

        ResponseEntity<Object> response = client.get("/x", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void non2xxWithBody_forwardsBody() {
        when(rest.exchange(eq("/x"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("err"));

        ResponseEntity<Object> response = client.get("/x", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("err");
    }

    @Test
    void non2xxWithoutBody_forwardsEmpty() {
        when(rest.exchange(eq("/x"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        ResponseEntity<Object> response = client.get("/x", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ArgumentCaptor<HttpEntity<?>> entityCaptor(HttpMethod method, String path) {
        ArgumentCaptor<HttpEntity<?>> captor = (ArgumentCaptor) ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq(path), eq(method), captor.capture(), eq(Object.class));
        return captor;
    }

    private org.springframework.http.HttpHeaders headerOfExchange() {
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(any(String.class), any(HttpMethod.class), captor.capture(), eq(Object.class));
        return captor.getValue().getHeaders();
    }
}
