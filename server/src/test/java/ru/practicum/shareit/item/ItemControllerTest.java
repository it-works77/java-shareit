package ru.practicum.shareit.item;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ItemIsNotAvailableException;
import ru.practicum.shareit.item.dto.CommentCreateRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemGetAllResponseDto;
import ru.practicum.shareit.item.dto.ItemGetByIdResponseDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateRequestDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    void add_whenValid_returnsOk() throws Exception {
        when(itemService.addByUserId(anyLong(), any(ItemCreateRequestDto.class)))
                .thenReturn(ItemResponseDto.builder().id(1L).name("Drill").build());

        mockMvc.perform(post("/items")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"d\",\"available\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void add_whenNoHeader_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"d\",\"available\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_whenFound_returnsOk() throws Exception {
        when(itemService.get(anyLong(), anyLong()))
                .thenReturn(ItemGetByIdResponseDto.builder().id(1L).name("Drill").comments(List.of()).build());

        mockMvc.perform(get("/items/1").header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void get_whenNotFound_returnsNotFound() throws Exception {
        when(itemService.get(anyLong(), anyLong())).thenThrow(new EntityNotFoundException("not found"));

        mockMvc.perform(get("/items/99").header(HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllByUserId_returnsOk() throws Exception {
        when(itemService.getAllByOwnerId(1L))
                .thenReturn(List.of(ItemGetAllResponseDto.builder().id(1L).build()));

        mockMvc.perform(get("/items").header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void search_whenTextPresent_returnsOk() throws Exception {
        when(itemService.search("drill"))
                .thenReturn(List.of(ItemResponseDto.builder().id(1L).build()));

        mockMvc.perform(get("/items/search").header(HEADER, 1L).param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void search_whenTextMissing_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/items/search").header(HEADER, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_whenValid_returnsOk() throws Exception {
        when(itemService.updateById(anyLong(), anyLong(), any(ItemUpdateRequestDto.class)))
                .thenReturn(ItemResponseDto.builder().id(1L).name("Saw").build());

        mockMvc.perform(patch("/items/1")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Saw\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Saw"));
    }

    @Test
    void delete_whenValid_returnsOk() throws Exception {
        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_whenValid_returnsOk() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any(CommentCreateRequestDto.class)))
                .thenReturn(CommentResponseDto.builder().id(1L).text("Nice").build());

        mockMvc.perform(post("/items/1/comment")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Nice\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Nice"));
    }

    @Test
    void addComment_whenNeverRented_returnsBadRequest() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any(CommentCreateRequestDto.class)))
                .thenThrow(new IllegalStateException("no rent"));

        mockMvc.perform(post("/items/1/comment")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Nice\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_whenItemUnavailable_returnsBadRequest() throws Exception {
        when(itemService.addByUserId(anyLong(), any(ItemCreateRequestDto.class)))
                .thenThrow(new ItemIsNotAvailableException("busy"));

        mockMvc.perform(post("/items")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"d\",\"available\":true}"))
                .andExpect(status().isBadRequest());
    }
}
