package ru.practicum.shareit.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void addByUserId_whenUserExists_returnsDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenAnswer(invocation -> {
            ItemRequest request = invocation.getArgument(0);
            request.setId(10L);
            return request;
        });

        var response = itemRequestService.addByUserId(1L, requestDto("Need a drill"));

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Need a drill", response.getDescription());
    }

    @Test
    void addByUserId_whenUserNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.addByUserId(99L, requestDto("x")));
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void getById_whenRequestHasItems_returnsDtoWithItems() {
        User requestor = user(1L);
        ItemRequest request = itemRequest(10L, requestor);
        Item item = item(100L, 2L, 10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(10L)).thenReturn(List.of(item));

        ItemRequestResponseDto response = itemRequestService.getById(1L, 10L);

        assertEquals(10L, response.getId());
        assertEquals(1, response.getItems().size());
        assertEquals(100L, response.getItems().get(0).getId());
    }

    @Test
    void getById_whenNoItems_returnsDtoWithEmptyItems() {
        User requestor = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findById(10L))
                .thenReturn(Optional.of(itemRequest(10L, requestor)));
        when(itemRepository.findByRequestId(10L)).thenReturn(List.of());

        ItemRequestResponseDto response = itemRequestService.getById(1L, 10L);

        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void getById_whenUserNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemRequestService.getById(99L, 10L));
    }

    @Test
    void getById_whenRequestNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemRequestService.getById(1L, 99L));
    }

    @Test
    void getAllByRequestorId_whenEmpty_returnsEmpty() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of());

        assertTrue(itemRequestService.getAllByRequestorId(1L).isEmpty());
        verify(itemRepository, never()).findAllByRequestIdIn(any());
    }

    @Test
    void getAllByRequestorId_whenRequestsExist_groupsItems() {
        User requestor = user(1L);
        ItemRequest first = itemRequest(10L, requestor);
        ItemRequest second = itemRequest(11L, requestor);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(first, second));
        when(itemRepository.findAllByRequestIdIn(List.of(10L, 11L)))
                .thenReturn(List.of(item(100L, 2L, 10L)));

        List<ItemRequestResponseDto> result = itemRequestService.getAllByRequestorId(1L);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getItems().size());
        assertTrue(result.get(1).getItems().isEmpty());
    }

    @Test
    void getAllByRequestorId_whenUserNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemRequestService.getAllByRequestorId(99L));
    }

    @Test
    void getAllNotMy_whenEmpty_returnsEmpty() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(1L)).thenReturn(List.of());

        assertTrue(itemRequestService.getAllNotMy(1L).isEmpty());
    }

    @Test
    void getAllNotMy_whenUserNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemRequestService.getAllNotMy(99L));
    }

    private static User user(Long id) {
        return User.builder().id(id).name("u" + id).email("u" + id + "@mail.com").build();
    }

    private static ItemRequest itemRequest(Long id, User requestor) {
        return ItemRequest.builder()
                .id(id)
                .description("Need stuff")
                .requestor(requestor)
                .created(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
    }

    private static Item item(Long id, Long ownerId, Long requestId) {
        return Item.builder().id(id).name("Drill").description("d").available(true)
                .ownerId(ownerId).requestId(requestId).build();
    }

    private static ItemRequestRequestDto requestDto(String description) {
        ItemRequestRequestDto dto = new ItemRequestRequestDto();
        dto.setDescription(description);
        return dto;
    }
}
