package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateRequestDto;
import ru.practicum.shareit.item.dto.ItemGetAllResponseDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemCreateRequestDto validCreateDto;
    private ItemUpdateRequestDto validUpdateDto;
    private ItemResponseDto responseDto;
    private ItemGetAllResponseDto responseDtoWithBookingDates;

    @BeforeEach
    void setUp() {
        validCreateDto = new ItemCreateRequestDto();
        validCreateDto.setName("Drill");
        validCreateDto.setDescription("Power drill");
        validCreateDto.setAvailable(true);

        validUpdateDto = new ItemUpdateRequestDto();
        validUpdateDto.setName("Updated Drill");

        responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build();

        responseDtoWithBookingDates = ItemGetAllResponseDto.builder()
                .id(1L)
                .name("Drill")
                .description("Power drill")
                .available(true)
                .lastBooking(null)
                .nextBooking(null)
                .build();
    }

    @Test
    void add_missingHeaderXSharerUserId_returns400() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Отсутствует обязательный заголовок"));
    }

    @Test
    void add_invalidHeaderXSharerUserIdNotLong_returns400() throws Exception {
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка парсинга запроса"));
    }

    @Test
    void add_invalidHeaderXSharerUserIdZero_returns400() throws Exception {
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.errors.userId").exists());
    }

    @Test
    void add_invalidHeaderXSharerUserIdNegative_returns400() throws Exception {
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, -1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.errors.userId").exists());
    }

    @Test
    void add_missingName_returns400() throws Exception {
        ItemCreateRequestDto dto = new ItemCreateRequestDto();
        dto.setDescription("Power drill");
        dto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void add_blankName_returns400() throws Exception {
        ItemCreateRequestDto dto = new ItemCreateRequestDto();
        dto.setName("");
        dto.setDescription("Power drill");
        dto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void add_missingDescription_returns400() throws Exception {
        ItemCreateRequestDto dto = new ItemCreateRequestDto();
        dto.setName("Drill");
        dto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.errors.description").exists());
    }

    @Test
    void add_blankDescription_returns400() throws Exception {
        ItemCreateRequestDto dto = new ItemCreateRequestDto();
        dto.setName("Drill");
        dto.setDescription("");
        dto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.errors.description").exists());
    }

    @Test
    void add_missingAvailable_returns400() throws Exception {
        ItemCreateRequestDto dto = new ItemCreateRequestDto();
        dto.setName("Drill");
        dto.setDescription("Power drill");

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.errors.available").exists());
    }

    @Test
    void add_invalidAvailableType_returns400() throws Exception {
        String json = "{\"name\":\"Drill\",\"description\":\"Power drill\",\"available\":\"yes\"}";

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка парсинга запроса"));
    }

    @Test
    void add_malformedJson_returns400() throws Exception {
        String malformedJson = "{\"name\":\"Drill\",\"description\":\"Power drill\"";

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка парсинга запроса"));
    }

    @Test
    void add_validRequest_returns200() throws Exception {
        when(itemService.addByUserId(anyLong(), any(ItemCreateRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.description").value("Power drill"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void get_invalidItemIdNotLong_returns400() throws Exception {
        mockMvc.perform(get("/items/abc").header(USER_ID_HEADER, 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка парсинга запроса"));
    }

    @Test
    void get_invalidItemIdZero_returns400() throws Exception {
        mockMvc.perform(get("/items/0").header(USER_ID_HEADER, 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.errors.itemId").exists());
    }

    @Test
    void get_invalidItemIdNegative_returns400() throws Exception {
        mockMvc.perform(get("/items/-1").header(USER_ID_HEADER, 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.errors.itemId").exists());
    }

    @Test
    void search_missingHeaderXSharerUserId_returns400() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Отсутствует обязательный заголовок"));
    }

    @Test
    void search_missingTextParam_returns400() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_invalidHeaderXSharerUserIdNotLong_returns400() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, "abc")
                        .param("text", "drill"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка парсинга запроса"));
    }

    @Test
    void search_validRequest_returns200() throws Exception {
        when(itemService.search(any()))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1)
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void getAllByUserId_missingHeaderXSharerUserId_returns400() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Отсутствует обязательный заголовок"));
    }

    @Test
    void getAllByUserId_invalidHeaderXSharerUserIdNotLong_returns400() throws Exception {
        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка парсинга запроса"));
    }

    @Test
    void getAllByUserId_invalidHeaderXSharerUserIdZero_returns400() throws Exception {
        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.errors.userId").exists());
    }

    @Test
    void getAllByUserId_validRequest_returns200() throws Exception {
        when(itemService.getAllByOwnerId(anyLong()))
                .thenReturn(List.of(responseDtoWithBookingDates));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void update_missingHeaderXSharerUserId_returns400() throws Exception {
        mockMvc.perform(patch("/items/{itemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Отсутствует обязательный заголовок"));
    }

    @Test
    void update_invalidItemIdNotLong_returns400() throws Exception {
        mockMvc.perform(patch("/items/abc")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка парсинга запроса"));
    }

    @Test
    void update_invalidItemIdZero_returns400() throws Exception {
        mockMvc.perform(patch("/items/0")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.errors.itemId").exists());
    }

    @Test
    void update_invalidHeaderXSharerUserIdNotLong_returns400() throws Exception {
        mockMvc.perform(patch("/items/{itemId}", 1)
                        .header(USER_ID_HEADER, "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка парсинга запроса"));
    }

    @Test
    void update_emptyBody_returns400() throws Exception {
        mockMvc.perform(patch("/items/{itemId}", 1)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void update_malformedJson_returns400() throws Exception {
        String malformedJson = "{\"name\":\"Drill\"";

        mockMvc.perform(patch("/items/{itemId}", 1)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка парсинга запроса"));
    }

    @Test
    void update_validRequest_returns200() throws Exception {
        when(itemService.updateById(anyLong(), anyLong(), any(ItemUpdateRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/items/{itemId}", 1)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void delete_invalidItemIdNotLong_returns400() throws Exception {
        mockMvc.perform(delete("/items/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка парсинга запроса"));
    }

    @Test
    void delete_invalidItemIdZero_returns400() throws Exception {
        mockMvc.perform(delete("/items/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.errors.itemId").exists());
    }

    @Test
    void delete_invalidItemIdNegative_returns400() throws Exception {
        mockMvc.perform(delete("/items/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.errors.itemId").exists());
    }

    @Test
    void delete_validRequest_returns200() throws Exception {
        mockMvc.perform(delete("/items/{itemId}", 1))
                .andExpect(status().isOk());
    }
}