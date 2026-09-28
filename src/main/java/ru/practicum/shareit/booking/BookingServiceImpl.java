package ru.practicum.shareit.booking;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingRequestState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
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
import java.util.Objects;

import static ru.practicum.shareit.booking.enums.BookingStatus.APPROVED;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto create(BookingCreateRequestDto bookingCreateRequestDto) {
        log.info("Creating booking: {}", bookingCreateRequestDto);
        if (bookingCreateRequestDto.getBookerId() == null) {
            throw new IllegalArgumentException("Booker ID is required");
        }

        User user = userRepository.findById(bookingCreateRequestDto.getBookerId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь-арендатор bookerId=" +
                        bookingCreateRequestDto.getBookerId() + " не найден"));

        Item item = itemRepository.findById(bookingCreateRequestDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Вещь itemId=" +
                        bookingCreateRequestDto.getItemId() + " не найдена"));

        // Проверка, что вещь доступна для аренды
        if (!item.getAvailable()) {
            throw new ItemIsNotAvailableException("Вещь itemId=" + item.getId() + " недоступна для аренды");
        }
        Booking booking = BookingMapper.mapBookingRequestDtoToBooking(bookingCreateRequestDto, item, user);

        bookingRepository.save(booking);
        log.info("Booking created: {}", booking);

        return BookingMapper.mapBookingToBookingResponseDto(booking);
    }

    @Override
    @Transactional
    public BookingResponseDto updateApprovement(Long ownerId, Long bookingId, Boolean approved) {
        log.info("Updating booking: bookingId={}, approved={}", bookingId, approved);
        if (ownerId == null) {
            throw new IllegalArgumentException("Booker ID is required");
        }


        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование bookingId=" + bookingId + " не найдено"));

        // Может быть выполнено только владельцем вещи
        if (!Objects.equals(booking.getItem().getOwnerId(), ownerId)) {
            throw new AccessDeniedException("Пользователь ownerId=" + ownerId + " не является владельцем вещи");
        }
        switch (booking.getStatus()) {
            case APPROVED -> throw new IllegalStateException("Бронирование уже подтверждено");
            case REJECTED -> throw new IllegalStateException("Бронирование уже отклонено");
            case CANCELED -> throw new IllegalStateException("Бронирование уже отменено");
        }

        if (approved == null) {
            throw new IllegalArgumentException("Approved status is required");
        }
        booking.setStatus(approved ? APPROVED : BookingStatus.REJECTED);
        bookingRepository.save(booking);

        //  Сервис должен не только позволять бронировать вещь на определённые даты,
        //  но и закрывать к ней доступ на время бронирования от других желающих.
        Item item = booking.getItem();
        item.setAvailable(false);
        itemRepository.save(item);

        log.info("Booking updated: {}", booking);
        log.info("Item updated: {}", item);
        return BookingMapper.mapBookingToBookingResponseDto(booking);
    }

    /**
     * Получение данных о конкретном бронировании (включая его статус). Может быть выполнено либо автором бронирования,
     * либо владельцем вещи, к которой относится бронирование.
     *
     * @param userId    ID of the user requesting the booking
     * @param bookingId ID of the booking to retrieve
     * @return the booking details as a {@link BookingResponseDto}
     * @throws IllegalArgumentException if {@code userId} or {@code bookingId} is {@code null}
     * @throws EntityNotFoundException  if no booking exists with the given {@code bookingId}
     * @throws AccessDeniedException    if the user is neither the booker nor the owner of the item
     */
    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getByUserIdAndBookingId(Long userId, Long bookingId) {
        log.info("Getting booking: userId={}, bookingId={}", userId, bookingId);
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID is required");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование bookingId=" + bookingId + " не найдено"));

        if (!(Objects.equals(booking.getBooker().getId(), userId) ||
                Objects.equals(booking.getItem().getOwnerId(), userId))) {
            throw new AccessDeniedException("Пользователь userId=" + userId +
                    " не является создателем бронирования или владельцем вещи");
        }
        log.info("Booking retrieved: {}", booking);
        return BookingMapper.mapBookingToBookingResponseDto(booking);
    }

    /**
     * Получение списка всех бронирований текущего пользователя.
     * Бронирования должны возвращаться отсортированными по дате от более новых к более старым.
     *
     * @param bookerId the ID of the booker whose bookings are to be retrieved
     * @param state    the booking state used to filter the results
     * @return a list of {@link BookingResponseDto} objects matching the booker and state
     * @throws IllegalArgumentException if {@code bookerId} or {@code state} is {@code null}
     * @throws EntityNotFoundException  if no user with the given {@code bookerId} exists
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllByBookerIdAndState(Long bookerId, BookingRequestState state) {
        log.info("Getting all bookings for booker: bookerId={}, state={}", bookerId, state);
        if (bookerId == null) {
            throw new IllegalArgumentException("Booker ID is required");
        }
        if (state == null) {
            throw new IllegalArgumentException("Booking state is required");
        }

        userRepository.findById(bookerId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь bookerId=" + bookerId + " не найден"));

        List<Booking> result = switch (state) {
            case ALL -> bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId);
            case CURRENT ->
                    bookingRepository.findAllCurrentByBookerIdAndStatus(bookerId, APPROVED, LocalDateTime.now());
            case PAST -> bookingRepository.findAllPastByBookerIdAndStatus(bookerId, APPROVED, LocalDateTime.now());
            case FUTURE -> bookingRepository.findAllFutureByBookerIdAndStatus(bookerId, APPROVED, LocalDateTime.now());
            case WAITING, REJECTED -> bookingRepository
                    .findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.valueOf(state.toString()));
        };

        return result
                .stream()
                .map(BookingMapper::mapBookingToBookingResponseDto)
                .toList();
    }

    /**
     * Получение списка бронирований для всех вещей текущего пользователя.
     *
     * @param ownerId the ID of the owner whose bookings are to be retrieved
     * @param state   the booking state to filter by
     * @return a list of booking response DTOs for the specified owner and state,
     * ordered by start date descending
     * @throws IllegalArgumentException if ownerId or state is null
     * @throws EntityNotFoundException  if no user with the given ownerId exists
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllByOwnerIdAndState(Long ownerId, BookingRequestState state) {
        log.info("Getting all bookings for owner: ownerId={}, state={}", ownerId, state);
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID is required");
        }
        if (state == null) {
            throw new IllegalArgumentException("Booking state is required");
        }

        userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь ownerId=" + ownerId + " не найден"));

        // Этот запрос имеет смысл для владельца хотя бы одной вещи.
        if (!itemRepository.existsByOwnerId(ownerId)) {
            throw new EntityNotFoundException("У пользователя ownerId=" + ownerId + " нет вещей");
        }

        List<Booking> result = switch (state) {
            case ALL -> bookingRepository.findAllByOwnerIdOrderByStartDesc(ownerId);
            case CURRENT -> bookingRepository.findAllCurrentByOwnerIdAndStatus(ownerId, APPROVED, LocalDateTime.now());
            case PAST -> bookingRepository.findAllPastByOwnerIdAndStatus(ownerId, APPROVED, LocalDateTime.now());
            case FUTURE -> bookingRepository.findAllFutureByOwnerIdAndStatus(ownerId, APPROVED, LocalDateTime.now());
            case WAITING, REJECTED -> bookingRepository
                    .findAllByOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.valueOf(state.toString()));
        };

        return result
                .stream()
                .map(BookingMapper::mapBookingToBookingResponseDto)
                .toList();
    }
}
