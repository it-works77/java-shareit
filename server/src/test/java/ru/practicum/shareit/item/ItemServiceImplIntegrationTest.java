package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static ru.practicum.shareit.booking.enums.BookingStatus.*;

import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.ItemGetAllResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplIntegrationTest {
    private final ItemServiceImpl itemService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getAllByOwnerId() {
        // Пользователи
        // - владелец вещи
        // - автор комментария с завершенным бронированием
        // - автор пары бронирований

        User itemOwner = saveUser("itemOwner", "itemOwner@email.com");
        User commentAuthor = saveUser("commentAuthor", "commentAuthor@email.com");
        User bookingAuthor = saveUser("bookingAuthor", "bookingAuthor@email.com");

        // Вещи
        // - без бронирований
        // - с бронированиями
        // - с комментариями
        Item itemWithoutBookings = saveItem(itemOwner, "item1", "itemWithoutBookings",
                true, null);
        Item itemWithBookings = saveItem(itemOwner, "item2", "itemWithBookings",
                true, null);
        Item itemWithComments = saveItem(itemOwner, "item3", "itemWithComments",
                true, null);

        /* Бронирования
            - для itemWithBookings
                - два завершенных, один отмененный
                - один APPROVED, один WAITING в будущем
            - для itemWithComments
                - один завершенный (к нему комментарии)
        */
        LocalDateTime now = LocalDateTime.now();

        Booking booking1 = saveBooking(bookingAuthor, itemWithBookings, APPROVED,
                now.minusHours(10), now.minusHours(9));
        // должен учесть это бронирование в LastBooking
        Booking booking2 = saveBooking(bookingAuthor, itemWithBookings, APPROVED,
                now.minusHours(8), now.minusHours(7));
        Booking booking3canceled = saveBooking(bookingAuthor, itemWithBookings, CANCELED,
                now.minusHours(6), now.minusHours(5));
        Booking booking3waiting = saveBooking(bookingAuthor, itemWithBookings, WAITING,
                now.minusHours(4), now.minusHours(3));
        // должен учесть это бронирование в NextBooking
        Booking booking4InFuture = saveBooking(bookingAuthor, itemWithBookings, APPROVED,
                now.plusHours(2), now.plusHours(3));

        Booking booking4WithComments = saveBooking(bookingAuthor, itemWithComments, APPROVED,
                now.minusHours(2), now.minusHours(1));

        // Комментарии
        Comment comment1 = saveComment(commentAuthor, itemWithComments, "comment1");

        List<ItemGetAllResponseDto> ownerItems = itemService.getAllByOwnerId(itemOwner.getId());

        assertNotNull(ownerItems);
        assertEquals(3, ownerItems.size());

        // Без бронирований
        ItemGetAllResponseDto ownerItemWithoutBookings = ownerItems.get(0);

        assertEquals(itemWithoutBookings.getId(), ownerItemWithoutBookings.getId());
        assertNull(ownerItemWithoutBookings.getLastBooking());
        assertNull(ownerItemWithoutBookings.getNextBooking());

        // С бронированиями
        ItemGetAllResponseDto ownerItemWithBookings = ownerItems.get(1);

        assertEquals(itemWithBookings.getId(), ownerItemWithBookings.getId());
        assertNotNull(ownerItemWithBookings.getLastBooking());
        assertNotNull(ownerItemWithBookings.getNextBooking());

        // Сравниваем с точностью до секунд (H2 возвращает наносекунды)
        assertEquals(booking2.getEnd().truncatedTo(ChronoUnit.SECONDS),
                ownerItemWithBookings.getLastBooking().truncatedTo(ChronoUnit.SECONDS));
        assertEquals(booking4InFuture.getStart().truncatedTo(ChronoUnit.SECONDS),
                ownerItemWithBookings.getNextBooking().truncatedTo(ChronoUnit.SECONDS));

        // С комментариями
        ItemGetAllResponseDto ownerItemWithComments = ownerItems.get(2);

        assertEquals(itemWithComments.getId(), ownerItemWithComments.getId());

        assertEquals(booking4WithComments.getEnd().truncatedTo(ChronoUnit.SECONDS),
                ownerItemWithComments.getLastBooking().truncatedTo(ChronoUnit.SECONDS));
        assertNull(ownerItemWithComments.getNextBooking());


        assertNotNull(ownerItemWithComments.getComments());
        assertEquals(1, ownerItemWithComments.getComments().size());
    }

    private Comment saveComment(User author, Item item, String text) {
        return commentRepository.save(Comment.builder()
                .author(author)
                .item(item)
                .text(text)
                .created(LocalDateTime.now())
                .build());
    }

    private Booking saveBooking(User booker, Item item, BookingStatus status, LocalDateTime start, LocalDateTime end) {
        return bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(item)
                .status(status)
                .start(start)
                .end(end)
                .build());
    }

    private User saveUser(String name, String email) {
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .build());
    }

    private Item saveItem(User owner, String name, String description, boolean isAvailable, ItemRequest itemRequest) {
        return itemRepository.save(Item.builder()
                .name(name)
                .description(description)
                .available(isAvailable)
                .ownerId(owner.getId())
                .requestId(itemRequest == null ? null : itemRequest.getId())
                .build());
    }
}