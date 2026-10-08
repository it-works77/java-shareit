package ru.practicum.shareit.item;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.CommentCreateRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplCommentTest {
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
    void addComment_whenCompletedBookingExists_returnsDto() {
        User author = user(1L, "Author");
        Item item = item(10L, 2L);
        Booking past = pastBooking(author, item);
        CommentCreateRequestDto request = commentRequest("Great drill");

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByBookerIdAndItemIdAndStatusAndEndBeforeOrderByEndDesc(
                eq(1L), eq(10L), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of(past));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(100L);
            return comment;
        });

        CommentResponseDto response = itemService.addComment(1L, 10L, request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Great drill", response.getText());
        assertEquals("Author", response.getAuthorName());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_whenUserNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> itemService.addComment(99L, 10L, commentRequest("x")));
    }

    @Test
    void addComment_whenItemNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "Author")));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> itemService.addComment(1L, 99L, commentRequest("x")));
    }

    @Test
    void addComment_whenNoCompletedBooking_throwsIllegalState() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "Author")));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item(10L, 2L)));
        when(bookingRepository.findAllByBookerIdAndItemIdAndStatusAndEndBeforeOrderByEndDesc(
                eq(1L), eq(10L), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThrows(IllegalStateException.class,
                () -> itemService.addComment(1L, 10L, commentRequest("x")));
    }

    @Test
    void addComment_whenOnlyNonApprovedPastBooking_throwsIllegalState() {
        // сервис спрашивает только APPROVED+прошлое: WAITING/CANCELED/будущее не проходят фильтр репозитория,
        // поэтому запрос возвращает пусто и комментарий запрещен
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "Author")));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item(10L, 2L)));
        when(bookingRepository.findAllByBookerIdAndItemIdAndStatusAndEndBeforeOrderByEndDesc(
                eq(1L), eq(10L), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThrows(IllegalStateException.class,
                () -> itemService.addComment(1L, 10L, commentRequest("x")));
    }

    private static User user(Long id, String name) {
        return User.builder().id(id).name(name).email(name + "@mail.com").build();
    }

    private static Item item(Long id, Long ownerId) {
        return Item.builder().id(id).name("Drill").description("d").available(true).ownerId(ownerId).build();
    }

    private static Booking pastBooking(User author, Item item) {
        return Booking.builder()
                .id(7L)
                .booker(author)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(2))
                .build();
    }

    private static CommentCreateRequestDto commentRequest(String text) {
        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        dto.setText(text);
        return dto;
    }
}
