package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemUpdateRequestDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemClient itemClient;

    @Autowired
    MockMvc mockMvc;

    private ItemCreateRequestDto validCreateDto;
    private ItemUpdateRequestDto validUpdateDto;
    private CommentCreateRequestDto validCommentDto;

    @BeforeEach
    void setUp() {
        validCreateDto = new ItemCreateRequestDto();
        validCreateDto.setName("Drill");
        validCreateDto.setDescription("Power drill");
        validCreateDto.setAvailable(true);

        validUpdateDto = new ItemUpdateRequestDto();
        validUpdateDto.setName("Updated Drill");

        validCommentDto = new CommentCreateRequestDto();
        validCommentDto.setText("Great item!");
    }

    // POST /items - Add Item
    @Test
    void add_whenValidRequest_shouldReturnOk() throws Exception {
        when(itemClient.add(anyLong(), any(ItemCreateRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isOk());

        verify(itemClient).add(1L, validCreateDto);
    }

    @Test
    void add_whenMissingHeaderXSharerUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).add(anyLong(), any());
    }

    @Test
    void add_whenHeaderXSharerUserIdNotLong_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).add(anyLong(), any());
    }

    @Test
    void add_whenHeaderXSharerUserIdZero_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).add(anyLong(), any());
    }

    @Test
    void add_whenMissingName_shouldReturnBadRequest() throws Exception {
        ItemCreateRequestDto dto = new ItemCreateRequestDto();
        dto.setDescription("Power drill");
        dto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).add(anyLong(), any());
    }

    @Test
    void add_whenBlankName_shouldReturnBadRequest() throws Exception {
        ItemCreateRequestDto dto = new ItemCreateRequestDto();
        dto.setName("");
        dto.setDescription("Power drill");
        dto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).add(anyLong(), any());
    }

    @Test
    void add_whenMissingDescription_shouldReturnBadRequest() throws Exception {
        ItemCreateRequestDto dto = new ItemCreateRequestDto();
        dto.setName("Drill");
        dto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).add(anyLong(), any());
    }

    @Test
    void add_whenBlankDescription_shouldReturnBadRequest() throws Exception {
        ItemCreateRequestDto dto = new ItemCreateRequestDto();
        dto.setName("Drill");
        dto.setDescription("");
        dto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).add(anyLong(), any());
    }

    @Test
    void add_whenMissingAvailable_shouldReturnBadRequest() throws Exception {
        ItemCreateRequestDto dto = new ItemCreateRequestDto();
        dto.setName("Drill");
        dto.setDescription("Power drill");

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).add(anyLong(), any());
    }

    @Test
    void add_whenInvalidAvailableType_shouldReturnBadRequest() throws Exception {
        String json = "{\"name\":\"Drill\",\"description\":\"Power drill\",\"available\":\"yes\"}";

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).add(anyLong(), any());
    }

    @Test
    void add_whenRequestIdNotPositive_shouldReturnBadRequest() throws Exception {
        ItemCreateRequestDto dto = new ItemCreateRequestDto();
        dto.setName("Drill");
        dto.setDescription("Power drill");
        dto.setAvailable(true);
        dto.setRequestId(0L);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).add(anyLong(), any());
    }

    @Test
    void add_whenMalformedJson_shouldReturnBadRequest() throws Exception {
        String malformedJson = "{\"name\":\"Drill\",\"description\":\"Power drill\"";

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    // GET /items/{itemId} - Get Item by ID
    @Test
    void get_whenValidRequest_shouldReturnOk() throws Exception {
        when(itemClient.getById(anyLong(), anyLong())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());

        verify(itemClient).getById(1L, 1L);
    }

    @Test
    void get_whenMissingHeaderXSharerUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/{itemId}", 1L))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getById(anyLong(), anyLong());
    }

    @Test
    void get_whenHeaderXSharerUserIdNotLong_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, "abc"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getById(anyLong(), anyLong());
    }

    @Test
    void get_whenHeaderXSharerUserIdZero_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, 0))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getById(anyLong(), anyLong());
    }

    @Test
    void get_whenItemIdNotLong_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/abc")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getById(anyLong(), anyLong());
    }

    @Test
    void get_whenItemIdZero_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/0")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getById(anyLong(), anyLong());
    }

    // GET /items - Get All Items by Owner
    @Test
    void getAllByUserId_whenValidRequest_shouldReturnOk() throws Exception {
        when(itemClient.getAllByOwnerId(anyLong())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());

        verify(itemClient).getAllByOwnerId(1L);
    }

    @Test
    void getAllByUserId_whenMissingHeaderXSharerUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getAllByOwnerId(anyLong());
    }

    @Test
    void getAllByUserId_whenHeaderXSharerUserIdNotLong_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, "abc"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getAllByOwnerId(anyLong());
    }

    @Test
    void getAllByUserId_whenHeaderXSharerUserIdZero_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 0))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).getAllByOwnerId(anyLong());
    }

    // GET /items/search - Search Items
    @Test
    void search_whenValidRequest_shouldReturnOk() throws Exception {
        when(itemClient.search(anyString())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1)
                        .param("text", "drill"))
                .andExpect(status().isOk());

        verify(itemClient).search("drill");
    }

    @Test
    void search_whenMissingHeaderXSharerUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).search(anyString());
    }

    @Test
    void search_whenHeaderXSharerUserIdNotLong_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, "abc")
                        .param("text", "drill"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).search(anyString());
    }

    @Test
    void search_whenMissingTextParam_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).search(anyString());
    }

    // PATCH /items/{itemId} - Update Item
    @Test
    void update_whenValidRequest_shouldReturnOk() throws Exception {
        when(itemClient.updateById(anyLong(), anyLong(), any(ItemUpdateRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateDto)))
                .andExpect(status().isOk());

        verify(itemClient).updateById(1L, 1L, validUpdateDto);
    }

    @Test
    void update_whenMissingHeaderXSharerUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).updateById(anyLong(), anyLong(), any());
    }

    @Test
    void update_whenHeaderXSharerUserIdNotLong_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).updateById(anyLong(), anyLong(), any());
    }

    @Test
    void update_whenHeaderXSharerUserIdZero_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).updateById(anyLong(), anyLong(), any());
    }

    @Test
    void update_whenItemIdNotLong_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/items/abc")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).updateById(anyLong(), anyLong(), any());
    }

    @Test
    void update_whenItemIdZero_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/items/0")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).updateById(anyLong(), anyLong(), any());
    }

    @Test
    void update_whenEmptyBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).updateById(anyLong(), anyLong(), any());
    }

    @Test
    void update_whenOnlyNameProvided_shouldReturnOk() throws Exception {
        ItemUpdateRequestDto dto = new ItemUpdateRequestDto();
        dto.setName("New Name");

        when(itemClient.updateById(anyLong(), anyLong(), any(ItemUpdateRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(itemClient).updateById(1L, 1L, dto);
    }

    @Test
    void update_whenOnlyDescriptionProvided_shouldReturnOk() throws Exception {
        ItemUpdateRequestDto dto = new ItemUpdateRequestDto();
        dto.setDescription("New Description");

        when(itemClient.updateById(anyLong(), anyLong(), any(ItemUpdateRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(itemClient).updateById(1L, 1L, dto);
    }

    @Test
    void update_whenOnlyAvailableProvided_shouldReturnOk() throws Exception {
        ItemUpdateRequestDto dto = new ItemUpdateRequestDto();
        dto.setAvailable(false);

        when(itemClient.updateById(anyLong(), anyLong(), any(ItemUpdateRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(itemClient).updateById(1L, 1L, dto);
    }

    @Test
    void update_whenMalformedJson_shouldReturnBadRequest() throws Exception {
        String malformedJson = "{\"name\":\"Drill\"";

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    // DELETE /items/{itemId} - Delete Item
    @Test
    void delete_whenValidRequest_shouldReturnOk() throws Exception {
        when(itemClient.remove(anyLong())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/items/{itemId}", 1L))
                .andExpect(status().isOk());

        verify(itemClient).remove(1L);
    }

    @Test
    void delete_whenItemIdNotLong_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/items/abc"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).remove(anyLong());
    }

    @Test
    void delete_whenItemIdZero_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/items/0"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).remove(anyLong());
    }

    // POST /items/{itemId}/comment - Add Comment
    @Test
    void addComment_whenValidRequest_shouldReturnOk() throws Exception {
        when(itemClient.addComment(anyLong(), anyLong(), any(CommentCreateRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCommentDto)))
                .andExpect(status().isOk());

        verify(itemClient).addComment(1L, 1L, validCommentDto);
    }

    @Test
    void addComment_whenMissingHeaderXSharerUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCommentDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).addComment(anyLong(), anyLong(), any());
    }

    @Test
    void addComment_whenHeaderXSharerUserIdNotLong_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(USER_ID_HEADER, "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCommentDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).addComment(anyLong(), anyLong(), any());
    }

    @Test
    void addComment_whenHeaderXSharerUserIdZero_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(USER_ID_HEADER, 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCommentDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).addComment(anyLong(), anyLong(), any());
    }

    @Test
    void addComment_whenItemIdNotLong_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items/abc/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCommentDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).addComment(anyLong(), anyLong(), any());
    }

    @Test
    void addComment_whenItemIdZero_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items/0/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCommentDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).addComment(anyLong(), anyLong(), any());
    }

    @Test
    void addComment_whenMissingText_shouldReturnBadRequest() throws Exception {
        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        dto.setText(null);

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).addComment(anyLong(), anyLong(), any());
    }

    @Test
    void addComment_whenMalformedJson_shouldReturnBadRequest() throws Exception {
        String malformedJson = "{\"text\":\"Great item!\"";

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }
}