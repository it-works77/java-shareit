package ru.practicum.shareit.request;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void add_whenValid_returnsOk() throws Exception {
        when(itemRequestService.addByUserId(anyLong(), any(ItemRequestRequestDto.class)))
                .thenReturn(ItemRequestCreateResponseDto.builder().id(1L).description("Need a drill").build());

        mockMvc.perform(post("/requests")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Need a drill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getById_whenFound_returnsOk() throws Exception {
        when(itemRequestService.getById(1L, 1L))
                .thenReturn(ItemRequestResponseDto.builder().id(1L).items(List.of()).build());

        mockMvc.perform(get("/requests/1").header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getById_whenNotFound_returnsNotFound() throws Exception {
        when(itemRequestService.getById(anyLong(), anyLong()))
                .thenThrow(new EntityNotFoundException("not found"));

        mockMvc.perform(get("/requests/99").header(HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllByRequesterId_returnsOk() throws Exception {
        when(itemRequestService.getAllByRequestorId(1L))
                .thenReturn(List.of(ItemRequestResponseDto.builder().id(1L).items(List.of()).build()));

        mockMvc.perform(get("/requests").header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getAllNotMy_returnsOk() throws Exception {
        when(itemRequestService.getAllNotMy(1L))
                .thenReturn(List.of(ItemRequestResponseDto.builder().id(2L).items(List.of()).build()));

        mockMvc.perform(get("/requests/all").header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L));
    }
}
