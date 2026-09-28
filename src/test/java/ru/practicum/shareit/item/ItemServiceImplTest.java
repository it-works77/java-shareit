package ru.practicum.shareit.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    @DisplayName("addByUserId: should save item and return DTO")
    void addByUserId_shouldSaveItemAndReturnDto() {
        Long userId = 1L;
        ItemCreateRequestDto request = buildCreateRequest("Дрель", "Ударная дрель", true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(10L);
            return item;
        });

        ItemResponseDto response = itemService.addByUserId(userId, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Дрель", response.getName());
        assertEquals("Ударная дрель", response.getDescription());
        assertEquals(Boolean.TRUE, response.getAvailable());

        verify(userRepository).findById(userId);

        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());

        Item savedItem = itemCaptor.getValue();
        assertEquals(userId, savedItem.getOwnerId());
        assertEquals("Дрель", savedItem.getName());
        assertEquals("Ударная дрель", savedItem.getDescription());
        assertEquals(Boolean.TRUE, savedItem.getAvailable());
    }


    @Test
    @DisplayName("addByUserId: should throw when user is not found")
    void addByUserId_whenUserNotFound_shouldThrow() {
        Long userId = 999L;
        ItemCreateRequestDto request = buildCreateRequest("Дрель", "Описание", true);

        doThrow(new EntityNotFoundException("Пользователь не найден"))
                .when(userRepository).findById(userId);

        assertThrows(EntityNotFoundException.class,
                () -> itemService.addByUserId(userId, request));

        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    @DisplayName("updateById: should throw when userId is null")
    void updateById_nullUserId_shouldThrow() {
        ItemUpdateRequestDto request = buildUpdateRequest("Новое имя", null, null);

        assertThrows(IllegalArgumentException.class,
                () -> itemService.updateById(null, 1L, request));

        verify(itemRepository, never()).findById(any());
    }

    @Test
    @DisplayName("updateById: should throw when itemId is null")
    void updateById_nullItemId_shouldThrow() {
        ItemUpdateRequestDto request = buildUpdateRequest("Новое имя", null, null);

        assertThrows(IllegalArgumentException.class,
                () -> itemService.updateById(1L, null, request));
    }

    @Test
    @DisplayName("updateById: should throw when item does not exist")
    void updateById_itemNotFound_shouldThrow() {
        Long userId = 1L;
        Long itemId = 99L;
        ItemUpdateRequestDto request = buildUpdateRequest("Новое имя", null, null);

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> itemService.updateById(userId, itemId, request));

        assertEquals("Вещь не найдена по id=99", exception.getMessage());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    @DisplayName("updateById: should throw when user does not own item")
    void updateById_notOwner_shouldThrow() {
        Long userId = 1L;
        Long itemId = 10L;
        Item existingItem = buildItem(itemId, 2L, "Старое имя", "Старое описание", true);
        ItemUpdateRequestDto request = buildUpdateRequest("Новое имя", null, null);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> itemService.updateById(userId, itemId, request));

        assertEquals("У пользователя userId=1 нет вещи с id=10", exception.getMessage());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    @DisplayName("updateById: should update all non-null fields")
    void updateById_shouldUpdateAllNonNullFields() {
        Long userId = 1L;
        Long itemId = 10L;
        Item existingItem = buildItem(itemId, userId, "Старое имя", "Старое описание", true);
        ItemUpdateRequestDto request = buildUpdateRequest("Новое имя", "Новое описание", false);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemResponseDto response = itemService.updateById(userId, itemId, request);

        assertNotNull(response);
        assertEquals("Новое имя", response.getName());
        assertEquals("Новое описание", response.getDescription());
        assertEquals(Boolean.FALSE, response.getAvailable());

        verify(itemRepository).save(existingItem);
        assertEquals("Новое имя", existingItem.getName());
        assertEquals("Новое описание", existingItem.getDescription());
        assertEquals(Boolean.FALSE, existingItem.getAvailable());
    }

    @Test
    @DisplayName("updateById: should update only non-null fields")
    void updateById_shouldUpdateOnlyNonNullFields() {
        Long userId = 1L;
        Long itemId = 10L;
        Item existingItem = buildItem(itemId, userId, "Старое имя", "Старое описание", true);
        ItemUpdateRequestDto request = buildUpdateRequest("Новое имя", null, null);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemResponseDto response = itemService.updateById(userId, itemId, request);

        assertEquals("Новое имя", response.getName());
        assertEquals("Старое описание", response.getDescription());
        assertEquals(Boolean.TRUE, response.getAvailable());
    }

    @Test
    @DisplayName("get: should return item by id")
    void get_shouldReturnItem() {
        Long itemId = 1L;
        Item item = buildItem(itemId, 1L, "Дрель", "Ударная дрель", true);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(itemId)).thenReturn(List.of());

        ItemGetByIdResponseDto response = itemService.get(itemId);

        assertNotNull(response);
        assertEquals(itemId, response.getId());
        assertEquals("Дрель", response.getName());
        assertEquals("Ударная дрель", response.getDescription());
        assertEquals(Boolean.TRUE, response.getAvailable());
    }

    @Test
    @DisplayName("get: should throw when item does not exist")
    void get_whenItemNotFound_shouldThrow() {
        Long itemId = 1L;

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> itemService.get(itemId));

        assertEquals("Вещь не найдена по id=1", exception.getMessage());
    }

    @Test
    @DisplayName("getAllByUserId: should return user items with last and next bookings")
    void getAllByUserId_shouldReturnUserItems() {
        Long ownerId = 1L;
        Item firstItem = buildItem(1L, ownerId, "Дрель", "Ударная дрель", true);
        Item secondItem = buildItem(2L, ownerId, "Отвертка", "Крестовая отвертка", false);
        List<Item> items = List.of(firstItem, secondItem);

        LocalDateTime lastBookingFirst = LocalDateTime.of(2025, 1, 1, 12, 0);
        LocalDateTime nextBookingFirst = LocalDateTime.of(2025, 2, 1, 12, 0);
        LocalDateTime lastBookingSecond = LocalDateTime.of(2025, 1, 15, 12, 0);

        when(itemRepository.getAllByOwnerId(ownerId)).thenReturn(items);

        when(bookingRepository.getLastBookingEndDateByItemId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(lastBookingFirst);
        when(bookingRepository.getNextBookingStartDateByItemId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(nextBookingFirst);
        when(bookingRepository.getLastBookingEndDateByItemId(eq(2L), any(LocalDateTime.class)))
                .thenReturn(lastBookingSecond);
        when(bookingRepository.getNextBookingStartDateByItemId(eq(2L), any(LocalDateTime.class)))
                .thenReturn(null);

        List<ItemGetAllResponseDto> response = itemService.getAllByUserId(ownerId);

        assertNotNull(response);
        assertEquals(2, response.size());

        ItemGetAllResponseDto first = response.get(0);
        assertEquals("Дрель", first.getName());
        assertEquals(Boolean.TRUE, first.getAvailable());
        assertEquals(lastBookingFirst, first.getLastBooking());
        assertEquals(nextBookingFirst, first.getNextBooking());

        ItemGetAllResponseDto second = response.get(1);
        assertEquals("Отвертка", second.getName());
        assertEquals(Boolean.FALSE, second.getAvailable());
        assertEquals(lastBookingSecond, second.getLastBooking());
        assertNull(second.getNextBooking());

        verify(itemRepository).getAllByOwnerId(ownerId);
    }


    @Test
    @DisplayName("search: should return empty list for blank text")
    void search_blankText_shouldReturnEmptyList() {
        List<ItemResponseDto> response = itemService.search("   ");

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(itemRepository, never()).findAllByText(anyString());
    }

    @Test
    @DisplayName("search: should return matching items")
    void search_shouldReturnMatchingItems() {
        String text = "дрель";
        List<Item> items = List.of(
                buildItem(1L, 1L, "Дрель", "Ударная дрель", true)
        );

        when(itemRepository.findAllByText(text)).thenReturn(items);

        List<ItemResponseDto> response = itemService.search(text);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Дрель", response.get(0).getName());

        verify(itemRepository).findAllByText(text);
    }

    @Test
    @DisplayName("remove: should delete existing item")
    void remove_shouldDeleteItem() {
        Long itemId = 1L;
        Item item = buildItem(itemId, 1L, "Дрель", "Ударная дрель", true);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        itemService.remove(itemId);

        verify(itemRepository).deleteById(itemId);
    }

    @Test
    @DisplayName("remove: should throw when item does not exist")
    void remove_whenItemNotFound_shouldThrow() {
        Long itemId = 1L;

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> itemService.remove(itemId));

        assertEquals("Вещь не найдена по id=1", exception.getMessage());
        verify(itemRepository, never()).deleteById(any());
    }

    private ItemCreateRequestDto buildCreateRequest(String name, String description, Boolean available) {
        ItemCreateRequestDto dto = new ItemCreateRequestDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        return dto;
    }

    private ItemUpdateRequestDto buildUpdateRequest(String name, String description, Boolean available) {
        ItemUpdateRequestDto dto = new ItemUpdateRequestDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        return dto;
    }

    private Item buildItem(Long id, Long ownerId, String name, String description, Boolean available) {
        Item item = new Item();
        item.setId(id);
        item.setOwnerId(ownerId);
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        return item;
    }
}
