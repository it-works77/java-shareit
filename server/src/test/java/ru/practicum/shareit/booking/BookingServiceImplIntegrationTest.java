package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingRequestState;
import ru.practicum.shareit.booking.enums.BookingStatus;

import static ru.practicum.shareit.booking.enums.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.enums.BookingStatus.CANCELED;
import static ru.practicum.shareit.booking.enums.BookingRequestState.*;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplIntegrationTest {
    private final BookingServiceImpl bookingService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Test
    void givenBookings_whenGetAllByOwnerIdAndDifferentStates_thenOk() {
        // Пользователи
        User itemOwner = saveUser("itemOwner", "itemOwner@email.com");
        User booker1 = saveUser("booker1", "booker1@email.com");
        User booker2 = saveUser("booker2", "booker2@email.com");
        User otherOwner = saveUser("otherOwner", "otherOwner@email.com");

        // Вещи владельца
        Item item1 = saveItem(itemOwner, "Item 1", "Description 1", true, null);
        Item item2 = saveItem(itemOwner, "Item 2", "Description 2", true, null);

        // Вещь другого владельца (не должна попасть в выборку)
        Item otherItem = saveItem(otherOwner, "Other Item", "Other Description", true, null);

        LocalDateTime now = LocalDateTime.now();

        // Бронирования для item1 (владелец itemOwner)
        // 1. PAST - завершенное APPROVED в прошлом
        Booking pastApproved = saveBooking(booker1, item1, APPROVED,
                now.minusDays(10), now.minusDays(9));

        // 2. CURRENT - текущее APPROVED (now внутри интервала)
        Booking currentApproved = saveBooking(booker1, item1, APPROVED,
                now.minusHours(2), now.plusHours(2));

        // 3. FUTURE - будущее APPROVED
        Booking futureApproved = saveBooking(booker2, item1, APPROVED,
                now.plusDays(2), now.plusDays(3));

        // 4. WAITING - в ожидании
        Booking waiting = saveBooking(booker1, item1, BookingStatus.WAITING,
                now.plusDays(5), now.plusDays(6));

        // 5. REJECTED - отклоненное
        Booking rejected = saveBooking(booker2, item1, BookingStatus.REJECTED,
                now.minusDays(5), now.minusDays(4));

        // 6. CANCELED - отмененное (не фильтруется через state, но есть в ALL)
        Booking canceled = saveBooking(booker1, item1, CANCELED,
                now.minusDays(3), now.minusDays(2));

        // Бронирования для otherItem (другого владельца) - не должны попасть в выборку
        saveBooking(booker1, otherItem, APPROVED, now.minusDays(1), now.plusDays(1));
        saveBooking(booker2, otherItem, BookingStatus.WAITING, now.plusDays(1), now.plusDays(2));

        // ===== Тестируем каждый state =====

        // ALL - все бронирования владельца (кроме других владельцев)
        List<BookingResponseDto> allBookings = bookingService.getAllByOwnerIdAndState(itemOwner.getId(), ALL);
        assertEquals(6, allBookings.size());
        assertBookingIdsContain(allBookings, pastApproved, currentApproved, futureApproved, waiting, rejected, canceled);

        // PAST - только завершенные APPROVED в прошлом
        List<BookingResponseDto> pastBookings = bookingService.getAllByOwnerIdAndState(itemOwner.getId(), PAST);
        assertEquals(1, pastBookings.size());
        assertEquals(pastApproved.getId(), pastBookings.get(0).getId());

        // CURRENT - только текущие APPROVED (now внутри интервала)
        List<BookingResponseDto> currentBookings = bookingService.getAllByOwnerIdAndState(itemOwner.getId(), CURRENT);
        assertEquals(1, currentBookings.size());
        assertEquals(currentApproved.getId(), currentBookings.get(0).getId());

        // FUTURE - только будущие APPROVED
        List<BookingResponseDto> futureBookings = bookingService.getAllByOwnerIdAndState(itemOwner.getId(), FUTURE);
        assertEquals(1, futureBookings.size());
        assertEquals(futureApproved.getId(), futureBookings.get(0).getId());

        // WAITING - только в статусе WAITING
        List<BookingResponseDto> waitingBookings = bookingService.getAllByOwnerIdAndState(itemOwner.getId(), BookingRequestState.WAITING);
        assertEquals(1, waitingBookings.size());
        assertEquals(waiting.getId(), waitingBookings.get(0).getId());
        assertEquals(BookingStatus.WAITING, waitingBookings.get(0).getStatus());

        // REJECTED - только в статусе REJECTED
        List<BookingResponseDto> rejectedBookings = bookingService.getAllByOwnerIdAndState(itemOwner.getId(), BookingRequestState.REJECTED);
        assertEquals(1, rejectedBookings.size());
        assertEquals(rejected.getId(), rejectedBookings.get(0).getId());
        assertEquals(BookingStatus.REJECTED, rejectedBookings.get(0).getStatus());

        // Проверка сортировки (по start DESC - от новых к старым)
        List<BookingResponseDto> allSorted = bookingService.getAllByOwnerIdAndState(itemOwner.getId(), ALL);
        for (int i = 0; i < allSorted.size() - 1; i++) {
            assertTrue(allSorted.get(i).getStart().isAfter(allSorted.get(i + 1).getStart())
                    || allSorted.get(i).getStart().isEqual(allSorted.get(i + 1).getStart()));
        }

        // Другой владелец видит только свои бронирования
        List<BookingResponseDto> otherOwnerBookings = bookingService.getAllByOwnerIdAndState(otherOwner.getId(), ALL);
        assertEquals(2, otherOwnerBookings.size());
    }

    private void assertBookingIdsContain(List<BookingResponseDto> list, Booking... expected) {
        for (Booking booking : expected) {
            assertTrue(list.stream().anyMatch(b -> b.getId().equals(booking.getId())),
                    "Expected booking id " + booking.getId() + " not found in list");
        }
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

    private Booking saveBooking(User booker, Item item, BookingStatus status, LocalDateTime start, LocalDateTime end) {
        return bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(item)
                .status(status)
                .start(start)
                .end(end)
                .build());
    }
}