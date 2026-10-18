package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemRequestClient itemRequestClient;

    @Autowired
    MockMvc mockMvc;

    @Test
    void add_shouldReturnCreatedRequest() throws Exception {
        ItemRequestRequestDto requestDto = new ItemRequestRequestDto();
        requestDto.setDescription("Нужна дрель");

        when(itemRequestClient.addByUserId(eq(1L), any(ItemRequestRequestDto.class)))
                .thenReturn(ResponseEntity.ok().body("{\"id\":1,\"description\":\"Нужна дрель\"}"));

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Нужна дрель")));

        verify(itemRequestClient).addByUserId(eq(1L), any(ItemRequestRequestDto.class));
    }

    @Test
    void add_whenDescriptionIsBlank_shouldReturnBadRequest() throws Exception {
        ItemRequestRequestDto requestDto = new ItemRequestRequestDto();
        requestDto.setDescription(" ");

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).addByUserId(any(), any());
    }

    @Test
    void add_whenUserIdHeaderIsMissing_shouldReturnBadRequest() throws Exception {
        ItemRequestRequestDto requestDto = new ItemRequestRequestDto();
        requestDto.setDescription("Нужна дрель");

        mockMvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).addByUserId(any(), any());
    }

    @Test
    void add_whenUserIdIsNegative_shouldReturnBadRequest() throws Exception {
        ItemRequestRequestDto requestDto = new ItemRequestRequestDto();
        requestDto.setDescription("Нужна дрель");

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, -1L)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).addByUserId(any(), any());
    }

    @Test
    void getById_shouldReturnRequest() throws Exception {
        when(itemRequestClient.getById(1L, 5L))
                .thenReturn(ResponseEntity.ok().body("{\"id\":5,\"description\":\"Нужна дрель\"}"));

        mockMvc.perform(get("/requests/{requestId}", 5L)
                        .header(USER_ID_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.description", is("Нужна дрель")));

        verify(itemRequestClient).getById(1L, 5L);
    }

    @Test
    void getAllByRequesterId_shouldReturnList() throws Exception {
        when(itemRequestClient.getAllByRequestorId(1L))
                .thenReturn(ResponseEntity.ok().body("[{\"id\":5,\"description\":\"Нужна дрель\"}]"));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(5)))
                .andExpect(jsonPath("$[0].description", is("Нужна дрель")));

        verify(itemRequestClient).getAllByRequestorId(1L);
    }

    @Test
    void getAllByRequesterId_whenHeaderIsMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).getAllByRequestorId(any());
    }

    @Test
    void getAllNotMy_shouldReturnList() throws Exception {
        when(itemRequestClient.getAllNotMy(1L))
                .thenReturn(ResponseEntity.ok().body("[{\"id\":7,\"description\":\"Нужен молоток\"}]"));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(7)))
                .andExpect(jsonPath("$[0].description", is("Нужен молоток")));

        verify(itemRequestClient).getAllNotMy(1L);
    }
}
