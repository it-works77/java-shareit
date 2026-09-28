package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingRequestState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ItemIsNotAvailableException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    // ---------- create ----------

    @Test
    void create_whenUserNotFound_shouldThrowEntityNotFoundException() {
        Long bookerId = 99L;
        BookingCreateRequestDto dto = createBookingRequest(bookerId, 1L);
        when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Пользователь-арендатор bookerId=" + bookerId + " не найден");

        verify(userRepository).findById(bookerId);
        verifyNoInteractions(itemRepository, bookingRepository);
    }

    @Test
    void create_whenItemNotFound_shouldThrowEntityNotFoundException() {
        User user = createUser(1L);
        Long itemId = 2L;
        BookingCreateRequestDto dto = createBookingRequest(1L, itemId);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Вещь itemId=" + itemId + " не найдена");

        verify(itemRepository).findById(itemId);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void create_whenItemNotAvailable_shouldThrowItemIsNotAvailableException() {
        User user = createUser(1L);
        Long itemId = 2L;
        Item item = createItem(itemId, 3L, false);
        BookingCreateRequestDto dto = createBookingRequest(1L, itemId);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.create(dto))
                .isInstanceOf(ItemIsNotAvailableException.class)
                .hasMessage("Вещь itemId=" + itemId + " недоступна для аренды");

        verifyNoInteractions(bookingRepository);
    }

    @Test
    void create_whenAllValid_shouldSaveBookingAndReturnDto() {
        User user = createUser(1L);
        Item item = createItem(2L, 3L, true);
        BookingCreateRequestDto dto = createBookingRequest(1L, 2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item));

        BookingResponseDto result = bookingService.create(dto);

        assertThat(result).isNotNull();
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        Booking savedBooking = bookingCaptor.getValue();

        assertThat(savedBooking.getBooker()).isEqualTo(user);
        assertThat(savedBooking.getItem()).isEqualTo(item);
        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(savedBooking.getStart()).isEqualTo(dto.getStart());
        assertThat(savedBooking.getEnd()).isEqualTo(dto.getEnd());
    }

    // ---------- updateApprovement ----------

    @Test
    void updateApprovement_whenBookingNotFound_shouldThrowEntityNotFoundException() {
        Long ownerId = 1L;
        Long bookingId = 99L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateApprovement(ownerId, bookingId, true))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Бронирование bookingId=" + bookingId + " не найдено");

        verify(bookingRepository).findById(bookingId);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void updateApprovement_whenUserIsNotOwner_shouldThrowAccessDeniedException() {
        Long ownerId = 1L;
        Long bookingId = 10L;
        Item item = createItem(2L, 2L, true);
        User booker = createUser(5L);
        Booking booking = createBooking(bookingId, item, booker, BookingStatus.WAITING);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateApprovement(ownerId, bookingId, true))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Пользователь ownerId=" + ownerId + " не является владельцем вещи");

        verifyNoInteractions(itemRepository);
    }

    @Test
    void updateApprovement_whenBookingAlreadyApproved_shouldThrowIllegalStateException() {
        Long ownerId = 1L;
        Long bookingId = 10L;
        Item item = createItem(2L, ownerId, true);
        User booker = createUser(5L);
        Booking booking = createBooking(bookingId, item, booker, BookingStatus.APPROVED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateApprovement(ownerId, bookingId, true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Бронирование недоступно для подтверждения");

        verify(bookingRepository, never()).save(any());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void updateApprovement_whenBookingAlreadyRejected_shouldThrowIllegalStateException() {
        Long ownerId = 1L;
        Long bookingId = 10L;
        Item item = createItem(2L, ownerId, true);
        User booker = createUser(5L);
        Booking booking = createBooking(bookingId, item, booker, BookingStatus.REJECTED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateApprovement(ownerId, bookingId, true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Бронирование недоступно для подтверждения");

        verify(bookingRepository, never()).save(any());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void updateApprovement_whenBookingAlreadyCanceled_shouldThrowIllegalStateException() {
        Long ownerId = 1L;
        Long bookingId = 10L;
        Item item = createItem(2L, ownerId, true);
        User booker = createUser(5L);
        Booking booking = createBooking(bookingId, item, booker, BookingStatus.CANCELED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateApprovement(ownerId, bookingId, true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Бронирование недоступно для подтверждения");

        verify(bookingRepository, never()).save(any());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void updateApprovement_whenApprovedIsNull_shouldThrowIllegalArgumentException() {
        Long ownerId = 1L;
        Long bookingId = 10L;
        Item item = createItem(2L, ownerId, true);
        User booker = createUser(5L);
        Booking booking = createBooking(bookingId, item, booker, BookingStatus.WAITING);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateApprovement(ownerId, bookingId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Approved status is required");

        verify(bookingRepository, never()).save(any());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void updateApprovement_whenApprovedTrue_shouldSetApprovedAndMakeItemUnavailable() {
        Long ownerId = 1L;
        Long bookingId = 10L;
        Item item = createItem(2L, ownerId, true);
        User booker = createUser(5L);
        Booking booking = createBooking(bookingId, item, booker, BookingStatus.WAITING);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.updateApprovement(ownerId, bookingId, true);

        assertThat(result).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(item.getAvailable()).isFalse();
        verify(bookingRepository).save(booking);
        verify(itemRepository).save(item);
    }

    @Test
    void updateApprovement_whenApprovedFalse_shouldSetRejectedAndMakeItemUnavailable() {
        Long ownerId = 1L;
        Long bookingId = 10L;
        Item item = createItem(2L, ownerId, true);
        User booker = createUser(5L);
        Booking booking = createBooking(bookingId, item, booker, BookingStatus.WAITING);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.updateApprovement(ownerId, bookingId, false);

        assertThat(result).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(item.getAvailable()).isFalse();
        verify(bookingRepository).save(booking);
        verify(itemRepository).save(item);
    }

    // ---------- getByUserIdAndBookingId ----------

    @Test
    void getByUserIdAndBookingId_whenBookingNotFound_shouldThrowEntityNotFoundException() {
        Long userId = 1L;
        Long bookingId = 99L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getByUserIdAndBookingId(userId, bookingId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Бронирование bookingId=" + bookingId + " не найдено");
    }

    @Test
    void getByUserIdAndBookingId_whenUserIsNotParticipant_shouldThrowAccessDeniedException() {
        Long userId = 3L;
        Long bookingId = 10L;
        Item item = createItem(2L, 2L, true);
        User booker = createUser(1L);
        Booking booking = createBooking(bookingId, item, booker, BookingStatus.APPROVED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getByUserIdAndBookingId(userId, bookingId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Пользователь userId=" + userId + " не является создателем бронирования или владельцем вещи");
    }

    @Test
    void getByUserIdAndBookingId_whenUserIsBooker_shouldReturnBooking() {
        Long userId = 1L;
        Long bookingId = 10L;
        Item item = createItem(2L, 2L, true);
        User booker = createUser(userId);
        Booking booking = createBooking(bookingId, item, booker, BookingStatus.APPROVED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getByUserIdAndBookingId(userId, bookingId);

        assertThat(result).isNotNull();
    }

    @Test
    void getByUserIdAndBookingId_whenUserIsOwner_shouldReturnBooking() {
        Long userId = 2L;
        Long bookingId = 10L;
        Item item = createItem(2L, userId, true);
        User booker = createUser(1L);
        Booking booking = createBooking(bookingId, item, booker, BookingStatus.APPROVED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getByUserIdAndBookingId(userId, bookingId);

        assertThat(result).isNotNull();
    }

    // ---------- getAllByBookerIdAndState ----------

    @Test
    void getAllByBookerIdAndState_whenUserNotFound_shouldThrowEntityNotFoundException() {
        Long bookerId = 99L;
        when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getAllByBookerIdAndState(bookerId, BookingRequestState.ALL))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Пользователь bookerId=" + bookerId + " не найден");

        verify(userRepository).findById(bookerId);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void getAllByBookerIdAndState_whenStateAll_shouldReturnAllBookings() {
        Long bookerId = 1L;
        User booker = createUser(bookerId);
        Item item = createItem(2L, 3L, true);
        Booking booking = createBooking(10L, item, booker, BookingStatus.APPROVED);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId)).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByBookerIdAndState(bookerId, BookingRequestState.ALL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        verify(bookingRepository).findAllByBookerIdOrderByStartDesc(bookerId);
    }

    @Test
    void getAllByBookerIdAndState_whenStateCurrent_shouldReturnCurrentBookings() {
        Long bookerId = 1L;
        User booker = createUser(bookerId);
        Item item = createItem(2L, 3L, true);
        Booking booking = createBooking(10L, item, booker, BookingStatus.APPROVED);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllCurrentByBookerIdAndStatus(
                eq(bookerId), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByBookerIdAndState(bookerId, BookingRequestState.CURRENT);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        verify(bookingRepository).findAllCurrentByBookerIdAndStatus(
                eq(bookerId), eq(BookingStatus.APPROVED), any(LocalDateTime.class));
    }

    @Test
    void getAllByBookerIdAndState_whenStatePast_shouldReturnPastBookings() {
        Long bookerId = 1L;
        User booker = createUser(bookerId);
        Item item = createItem(2L, 3L, true);
        Booking booking = createBooking(10L, item, booker, BookingStatus.APPROVED);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatusAndEndBeforeOrderByStartDesc(
                eq(bookerId), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByBookerIdAndState(bookerId, BookingRequestState.PAST);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        verify(bookingRepository).findAllByBookerIdAndStatusAndEndBeforeOrderByStartDesc(
                eq(bookerId), eq(BookingStatus.APPROVED), any(LocalDateTime.class));
    }

    @Test
    void getAllByBookerIdAndState_whenStateFuture_shouldReturnFutureBookings() {
        Long bookerId = 1L;
        User booker = createUser(bookerId);
        Item item = createItem(2L, 3L, true);
        Booking booking = createBooking(10L, item, booker, BookingStatus.APPROVED);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatusAndStartAfterOrderByStartDesc(
                eq(bookerId), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByBookerIdAndState(bookerId, BookingRequestState.FUTURE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        verify(bookingRepository).findAllByBookerIdAndStatusAndStartAfterOrderByStartDesc(
                eq(bookerId), eq(BookingStatus.APPROVED), any(LocalDateTime.class));
    }

    @Test
    void getAllByBookerIdAndState_whenStateWaiting_shouldReturnWaitingBookings() {
        Long bookerId = 1L;
        User booker = createUser(bookerId);
        Item item = createItem(2L, 3L, true);
        Booking booking = createBooking(10L, item, booker, BookingStatus.WAITING);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByBookerIdAndState(bookerId, BookingRequestState.WAITING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        verify(bookingRepository).findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING);
    }

    @Test
    void getAllByBookerIdAndState_whenStateRejected_shouldReturnRejectedBookings() {
        Long bookerId = 1L;
        User booker = createUser(bookerId);
        Item item = createItem(2L, 3L, true);
        Booking booking = createBooking(10L, item, booker, BookingStatus.REJECTED);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByBookerIdAndState(bookerId, BookingRequestState.REJECTED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        verify(bookingRepository).findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED);
    }

    // ---------- getAllByOwnerIdAndState ----------

    @Test
    void getAllByOwnerIdAndState_whenUserNotFound_shouldThrowEntityNotFoundException() {
        Long ownerId = 99L;
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getAllByOwnerIdAndState(ownerId, BookingRequestState.ALL))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Пользователь ownerId=" + ownerId + " не найден");

        verify(userRepository).findById(ownerId);
        verifyNoInteractions(itemRepository, bookingRepository);
    }

    @Test
    void getAllByOwnerIdAndState_whenUserHasNoItems_shouldThrowEntityNotFoundException() {
        Long ownerId = 1L;
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(createUser(ownerId)));
        when(itemRepository.existsByOwnerId(ownerId)).thenReturn(false);

        assertThatThrownBy(() -> bookingService.getAllByOwnerIdAndState(ownerId, BookingRequestState.ALL))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("У пользователя ownerId=" + ownerId + " нет вещей");

        verify(itemRepository).existsByOwnerId(ownerId);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void getAllByOwnerIdAndState_whenStateAll_shouldReturnAllOwnerBookings() {
        Long ownerId = 1L;
        User owner = createUser(ownerId);
        Item item = createItem(2L, ownerId, true);
        User booker = createUser(5L);
        Booking booking = createBooking(10L, item, booker, BookingStatus.APPROVED);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.existsByOwnerId(ownerId)).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdOrderByStartDesc(ownerId)).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByOwnerIdAndState(ownerId, BookingRequestState.ALL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        verify(bookingRepository).findAllByOwnerIdOrderByStartDesc(ownerId);
    }

    @Test
    void getAllByOwnerIdAndState_whenStateCurrent_shouldReturnCurrentOwnerBookings() {
        Long ownerId = 1L;
        User owner = createUser(ownerId);
        Item item = createItem(2L, ownerId, true);
        User booker = createUser(5L);
        Booking booking = createBooking(10L, item, booker, BookingStatus.APPROVED);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.existsByOwnerId(ownerId)).thenReturn(true);
        when(bookingRepository.findAllCurrentByOwnerIdAndStatus(
                eq(ownerId), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByOwnerIdAndState(ownerId, BookingRequestState.CURRENT);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        verify(bookingRepository).findAllCurrentByOwnerIdAndStatus(
                eq(ownerId), eq(BookingStatus.APPROVED), any(LocalDateTime.class));
    }

    @Test
    void getAllByOwnerIdAndState_whenStatePast_shouldReturnPastOwnerBookings() {
        Long ownerId = 1L;
        User owner = createUser(ownerId);
        Item item = createItem(2L, ownerId, true);
        User booker = createUser(5L);
        Booking booking = createBooking(10L, item, booker, BookingStatus.APPROVED);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.existsByOwnerId(ownerId)).thenReturn(true);
        when(bookingRepository.findAllPastByOwnerIdAndStatus(
                eq(ownerId), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByOwnerIdAndState(ownerId, BookingRequestState.PAST);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        verify(bookingRepository).findAllPastByOwnerIdAndStatus(
                eq(ownerId), eq(BookingStatus.APPROVED), any(LocalDateTime.class));
    }

    @Test
    void getAllByOwnerIdAndState_whenStateFuture_shouldReturnFutureOwnerBookings() {
        Long ownerId = 1L;
        User owner = createUser(ownerId);
        Item item = createItem(2L, ownerId, true);
        User booker = createUser(5L);
        Booking booking = createBooking(10L, item, booker, BookingStatus.APPROVED);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.existsByOwnerId(ownerId)).thenReturn(true);
        when(bookingRepository.findAllFutureByOwnerIdAndStatus(
                eq(ownerId), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByOwnerIdAndState(ownerId, BookingRequestState.FUTURE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        verify(bookingRepository).findAllFutureByOwnerIdAndStatus(
                eq(ownerId), eq(BookingStatus.APPROVED), any(LocalDateTime.class));
    }

    @Test
    void getAllByOwnerIdAndState_whenStateWaiting_shouldReturnWaitingOwnerBookings() {
        Long ownerId = 1L;
        User owner = createUser(ownerId);
        Item item = createItem(2L, ownerId, true);
        User booker = createUser(5L);
        Booking booking = createBooking(10L, item, booker, BookingStatus.WAITING);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.existsByOwnerId(ownerId)).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByOwnerIdAndState(ownerId, BookingRequestState.WAITING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        verify(bookingRepository).findAllByOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING);
    }

    @Test
    void getAllByOwnerIdAndState_whenStateRejected_shouldReturnRejectedOwnerBookings() {
        Long ownerId = 1L;
        User owner = createUser(ownerId);
        Item item = createItem(2L, ownerId, true);
        User booker = createUser(5L);
        Booking booking = createBooking(10L, item, booker, BookingStatus.REJECTED);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.existsByOwnerId(ownerId)).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByOwnerIdAndState(ownerId, BookingRequestState.REJECTED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNotNull();
        verify(bookingRepository).findAllByOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED);
    }


    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("user" + id);
        user.setEmail("user" + id + "@example.com");
        return user;
    }

    private Item createItem(Long id, Long ownerId, boolean available) {
        Item item = new Item();
        item.setId(id);
        item.setOwnerId(ownerId);
        item.setAvailable(available);
        item.setName("item" + id);
        item.setDescription("description" + id);
        return item;
    }

    private Booking createBooking(Long id, Item item, User booker, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        return booking;
    }

    private BookingCreateRequestDto createBookingRequest(Long bookerId, Long itemId) {
        return BookingCreateRequestDto.builder()
                .bookerId(bookerId)
                .itemId(itemId)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();
    }
}
