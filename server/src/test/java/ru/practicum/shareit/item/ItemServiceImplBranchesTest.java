package ru.practicum.shareit.item;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemUpdateRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplBranchesTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void get_whenUserNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.get(99L, 1L));
        verify(itemRepository, never()).findById(1L);
    }

    @Test
    void updateById_whenUserNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> itemService.updateById(99L, 1L, updateRequest("x")));
        verify(itemRepository, never()).findById(1L);
    }

    @Test
    void getAllByOwnerId_whenUserNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.getAllByOwnerId(99L));
        verify(itemRepository, never()).getAllByOwnerId(99L);
    }

    @Test
    void getAllByOwnerId_whenNoItems_returnsEmptyWithoutExtraQueries() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRepository.getAllByOwnerId(1L)).thenReturn(List.of());

        List<?> result = itemService.getAllByOwnerId(1L);

        assertTrue(result.isEmpty());
        verify(commentRepository, never()).findAllByItemOwnerIdOrderByCreatedDesc(1L);
        verify(bookingRepository, never()).findAllLastBookingByStatus(eq(1L), anyString(), any());
    }

    @Test
    void get_whenNotOwner_returnsDtoWithoutBookings() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item(10L, 2L)));
        when(commentRepository.findAllByItemId(10L)).thenReturn(List.of());

        var result = itemService.get(1L, 10L);

        assertEquals(10L, result.getId());
        verify(bookingRepository, never()).getLastBookingEndDateByItemId(anyLong(), any());
    }

    private static User user(Long id) {
        return User.builder().id(id).name("u" + id).email("u" + id + "@mail.com").build();
    }

    private static Item item(Long id, Long ownerId) {
        return Item.builder().id(id).name("Drill").description("d").available(true).ownerId(ownerId).build();
    }

    private static ItemUpdateRequestDto updateRequest(String name) {
        ItemUpdateRequestDto dto = new ItemUpdateRequestDto();
        dto.setName(name);
        return dto;
    }
}
