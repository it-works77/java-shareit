package ru.practicum.shareit.booking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemLastBookingProjection;
import ru.practicum.shareit.item.dto.ItemNextBookingProjection;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findAllByBookerId_filtersByBookerAndStatusSorted() {
        Fixture fixture = fixture();
        booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                hoursAgo(10), hoursAgo(9));
        Booking newer = booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                hoursAgo(8), hoursAgo(7));
        booking(fixture.booker, fixture.item, BookingStatus.WAITING,
                hoursAgo(8), hoursAgo(7));
        booking(fixture.other, fixture.item, BookingStatus.APPROVED,
                hoursAgo(8), hoursAgo(7));

        List<Booking> result = bookingRepository
                .findAllByBookerIdAndStatusOrderByStartDesc(fixture.booker.getId(), BookingStatus.APPROVED);

        assertEquals(2, result.size());
        assertEquals(newer.getId(), result.get(0).getId());
    }

    @Test
    void findAllByBookerId_returnsAllStatuses() {
        Fixture fixture = fixture();
        booking(fixture.booker, fixture.item, BookingStatus.APPROVED, hoursAgo(10), hoursAgo(9));
        booking(fixture.booker, fixture.item, BookingStatus.WAITING, hoursAgo(8), hoursAgo(7));

        assertEquals(2, bookingRepository.findAllByBookerIdOrderByStartDesc(fixture.booker.getId()).size());
        assertTrue(bookingRepository.findAllByBookerIdOrderByStartDesc(fixture.other.getId()).isEmpty());
    }

    @Test
    void findAllCurrentByBookerId_returnsBetween() {
        Fixture fixture = fixture();
        LocalDateTime now = LocalDateTime.now();
        Booking current = booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.minusHours(1), now.plusHours(1));
        booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.minusHours(5), now.minusHours(4));
        booking(fixture.booker, fixture.item, BookingStatus.WAITING,
                now.minusHours(1), now.plusHours(1));

        List<Booking> result = bookingRepository.findAllCurrentByBookerIdAndStatus(
                fixture.booker.getId(), BookingStatus.APPROVED, now);

        assertEquals(1, result.size());
        assertEquals(current.getId(), result.get(0).getId());
    }

    @Test
    void findAllByBookerIdPastAndFuture_filtersByTime() {
        Fixture fixture = fixture();
        LocalDateTime now = LocalDateTime.now();
        Booking past = booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.minusDays(3), now.minusDays(2));
        Booking future = booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.plusDays(2), now.plusDays(3));
        booking(fixture.booker, fixture.item, BookingStatus.WAITING,
                now.minusDays(3), now.minusDays(2));

        List<Booking> pastResult = bookingRepository
                .findAllByBookerIdAndStatusAndEndBeforeOrderByStartDesc(
                        fixture.booker.getId(), BookingStatus.APPROVED, now);
        List<Booking> futureResult = bookingRepository
                .findAllByBookerIdAndStatusAndStartAfterOrderByStartDesc(
                        fixture.booker.getId(), BookingStatus.APPROVED, now);

        assertEquals(List.of(past.getId()), pastResult.stream().map(Booking::getId).toList());
        assertEquals(List.of(future.getId()), futureResult.stream().map(Booking::getId).toList());
    }

    @Test
    void findAllByOwner_filtersByOwnerSorted() {
        Fixture fixture = fixture();
        LocalDateTime now = LocalDateTime.now();
        Booking first = booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.minusDays(2), now.minusDays(1));
        booking(fixture.booker, fixture.otherItem, BookingStatus.APPROVED,
                now.minusDays(2), now.minusDays(1));

        List<Booking> all = bookingRepository.findAllByOwnerIdOrderByStartDesc(fixture.owner.getId());
        List<Booking> waiting = bookingRepository.findAllByOwnerIdAndStatusOrderByStartDesc(
                fixture.owner.getId(), BookingStatus.WAITING);

        assertEquals(List.of(first.getId()), all.stream().map(Booking::getId).toList());
        assertTrue(waiting.isEmpty());
    }

    @Test
    void findCurrentPastFutureByOwner_filtersByTimeAndStatus() {
        Fixture fixture = fixture();
        LocalDateTime now = LocalDateTime.now();
        Booking past = booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.minusDays(4), now.minusDays(3));
        Booking current = booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.minusHours(1), now.plusHours(1));
        Booking future = booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.plusDays(3), now.plusDays(4));
        booking(fixture.booker, fixture.item, BookingStatus.REJECTED,
                now.minusDays(4), now.minusDays(3));

        assertEquals(List.of(past.getId()), ids(bookingRepository.findAllPastByOwnerIdAndStatus(
                fixture.owner.getId(), BookingStatus.APPROVED, now)));
        assertEquals(List.of(current.getId()), ids(bookingRepository.findAllCurrentByOwnerIdAndStatus(
                fixture.owner.getId(), BookingStatus.APPROVED, now)));
        assertEquals(List.of(future.getId()), ids(bookingRepository.findAllFutureByOwnerIdAndStatus(
                fixture.owner.getId(), BookingStatus.APPROVED, now)));
    }

    @Test
    void getLastAndNextByItemId_returnsMaxMinAndNull() {
        Fixture fixture = fixture();
        LocalDateTime now = LocalDateTime.now();
        booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.minusDays(10), now.minusDays(9));
        Booking last = booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.minusDays(8), now.minusDays(7));
        Booking next = booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.plusDays(2), now.plusDays(3));

        assertEquals(last.getEnd().withNano(0),
                bookingRepository.getLastBookingEndDateByItemId(fixture.item.getId(), now).withNano(0));
        assertEquals(next.getStart().withNano(0),
                bookingRepository.getNextBookingStartDateByItemId(fixture.item.getId(), now).withNano(0));
        assertNull(bookingRepository.getLastBookingEndDateByItemId(fixture.otherItem.getId(), now));
        assertNull(bookingRepository.getNextBookingStartDateByItemId(fixture.otherItem.getId(), now));
    }

    @Test
    void findAllLastAndNextBookingByStatus_projectionsAndStatusFilter() {
        Fixture fixture = fixture();
        LocalDateTime now = LocalDateTime.now();
        Booking past = booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.minusDays(8), now.minusDays(7));
        Booking future = booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.plusDays(2), now.plusDays(3));
        booking(fixture.booker, fixture.item, BookingStatus.WAITING,
                now.minusDays(6), now.minusDays(5));

        List<ItemLastBookingProjection> last = bookingRepository.findAllLastBookingByStatus(
                fixture.owner.getId(), BookingStatus.APPROVED.name(), now);
        List<ItemNextBookingProjection> next = bookingRepository.findAllNextBookingByStatus(
                fixture.owner.getId(), BookingStatus.APPROVED.name(), now);

        assertEquals(1, last.size());
        assertEquals(fixture.item.getId(), last.get(0).getId());
        assertEquals(past.getEnd().withNano(0), last.get(0).getLastBooking().withNano(0));
        assertEquals(1, next.size());
        assertEquals(future.getStart().withNano(0), next.get(0).getNextBooking().withNano(0));
        assertTrue(bookingRepository.findAllLastBookingByStatus(
                fixture.stranger.getId(), BookingStatus.APPROVED.name(), now).isEmpty());
    }

    @Test
    void findAllByBookerIdAndItemId_returnsOrdered() {
        Fixture fixture = fixture();
        LocalDateTime now = LocalDateTime.now();
        booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.minusDays(10), now.minusDays(9));
        Booking newer = booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.minusDays(8), now.minusDays(7));

        List<Booking> result = bookingRepository
                .findAllByBookerIdAndItemIdAndStatusAndEndBeforeOrderByEndDesc(
                        fixture.booker.getId(), fixture.item.getId(), BookingStatus.APPROVED, now);

        assertEquals(2, result.size());
        assertEquals(newer.getId(), result.get(0).getId());
        assertTrue(bookingRepository.findAllByBookerIdAndItemIdAndStatusAndEndBeforeOrderByEndDesc(
                fixture.booker.getId(), fixture.otherItem.getId(), BookingStatus.APPROVED, now).isEmpty());
    }

    @Test
    void existsByItemIdAndStatus_overlapTrueAndFalse() {
        Fixture fixture = fixture();
        LocalDateTime now = LocalDateTime.now();
        booking(fixture.booker, fixture.item, BookingStatus.APPROVED,
                now.plusDays(2), now.plusDays(4));

        assertTrue(bookingRepository.existsByItemIdAndStatusAndStartBeforeAndEndAfter(
                fixture.item.getId(), BookingStatus.APPROVED, now.plusDays(3), now.plusDays(1)));
        assertFalse(bookingRepository.existsByItemIdAndStatusAndStartBeforeAndEndAfter(
                fixture.item.getId(), BookingStatus.APPROVED, now.plusDays(6), now.plusDays(5)));
        assertFalse(bookingRepository.existsByItemIdAndStatusAndStartBeforeAndEndAfter(
                fixture.item.getId(), BookingStatus.WAITING, now.plusDays(3), now.plusDays(1)));
    }

    private Fixture fixture() {
        User owner = user("owner");
        User booker = user("booker");
        User other = user("other");
        User stranger = user("stranger");
        Item item = item(owner, "Drill");
        Item otherItem = item(user("second-owner"), "Saw");
        return new Fixture(owner, booker, other, stranger, item, otherItem);
    }

    private record Fixture(User owner, User booker, User other, User stranger, Item item, Item otherItem) {
    }

    private static List<Long> ids(List<Booking> bookings) {
        return bookings.stream().map(Booking::getId).toList();
    }

    private static LocalDateTime hoursAgo(int hours) {
        return LocalDateTime.now().minusHours(hours);
    }

    private User user(String name) {
        return userRepository.save(User.builder().name(name).email(name + "@mail.com").build());
    }

    private Item item(User owner, String name) {
        return itemRepository.save(Item.builder()
                .name(name)
                .description("d")
                .available(true)
                .ownerId(owner.getId())
                .build());
    }

    private Booking booking(User booker, Item item, BookingStatus status, LocalDateTime start, LocalDateTime end) {
        return bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(item)
                .status(status)
                .start(start)
                .end(end)
                .build());
    }
}
