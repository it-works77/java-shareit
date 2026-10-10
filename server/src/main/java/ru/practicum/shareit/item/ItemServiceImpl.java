package ru.practicum.shareit.item;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemResponseDto addByUserId(Long userId, ItemCreateRequestDto itemCreateRequestDto) {
        log.info("Add item: itemDto: {}", itemCreateRequestDto);
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден по id=%s"
                        .formatted(userId)));

        if(itemCreateRequestDto.getRequestId() != null) {
            itemRequestRepository.findById(itemCreateRequestDto.getRequestId())
                    .orElseThrow(() -> new EntityNotFoundException("Запрос не найден по id=%s"
                            .formatted(itemCreateRequestDto.getRequestId())));
        }

        Item item = ItemMapper.mapItemCreateRequestDtoToItem(itemCreateRequestDto);
        item.setOwnerId(userId);

        itemRepository.save(item);
        return ItemMapper.mapItemToItemResponseDto(item);
    }

    @Override
    @Transactional
    public ItemResponseDto updateById(Long userId, Long itemId, ItemUpdateRequestDto itemUpdateRequestDto) {

        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден по id=%s"
                        .formatted(userId)));

        Item existingItem = itemRepository.findById(itemId).orElseThrow(
                () -> new EntityNotFoundException("Вещь не найдена по id=%s".formatted(itemId)));

        if (!Objects.equals(userId, existingItem.getOwnerId())) {
            throw new EntityNotFoundException("У пользователя userId=%s нет вещи с id=%s"
                    .formatted(userId, itemId));
        }

        // Обновляем только не null поля
        if (itemUpdateRequestDto.getName() != null) {
            existingItem.setName(itemUpdateRequestDto.getName());
        }

        if (itemUpdateRequestDto.getDescription() != null) {
            existingItem.setDescription(itemUpdateRequestDto.getDescription());
        }

        if (itemUpdateRequestDto.getAvailable() != null) {
            existingItem.setAvailable(itemUpdateRequestDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.mapItemToItemResponseDto(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemGetByIdResponseDto get(Long userId, Long id) {
        log.info("Get item. ItemId={}", id);

        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден по id=%s"
                        .formatted(userId)));

        Item item = itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Вещь не найдена по id=%s".formatted(id)));

        List<Comment> comments = commentRepository.findAllByItemId(id);

        // Предлагаю реализовать крайние бронирования и в GET /items и в GET /items/{itemId}
        var now = LocalDateTime.now();
        ItemGetByIdResponseDto result = ItemMapper.mapItemToItemGetByIdResponseDto(item,
                comments.stream().map(CommentMapper::mapCommentToCommentResponseDto).toList());

        // даты должен видеть только владелец вещи
        if (Objects.equals(item.getOwnerId(), userId)) {
            result.setLastBooking(bookingRepository.getLastBookingEndDateByItemId(item.getId(), now));
            result.setNextBooking(bookingRepository.getNextBookingStartDateByItemId(item.getId(), now));
        }
        return result;

    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemGetAllResponseDto> getAllByOwnerId(Long ownerId) {
        log.info("Get owner's items. UserId={}", ownerId);

        userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден по id=%s"
                        .formatted(ownerId)));

        List<Item> ownerItems = itemRepository.getAllByOwnerId(ownerId);

        if (ownerItems.isEmpty()) {
            return List.of();
        }

        // Получаем даты предыдущего и следующего бронирований, все комментарии к вещам владельца
        List<Comment> ownerItemComments = commentRepository.findAllByItemOwnerIdOrderByCreatedDesc(ownerId);

        LocalDateTime now = LocalDateTime.now();
        List<ItemLastBookingProjection> ownerItemsLastBookings = bookingRepository
                .findAllLastBookingByStatus(ownerId, BookingStatus.APPROVED.name(), now);

        List<ItemNextBookingProjection> ownerItemsNextBookings = bookingRepository
                .findAllNextBookingByStatus(ownerId, BookingStatus.APPROVED.name(), now);

        // Раскладываем по itemId
        Map<Long, LocalDateTime> lastBookingByItemId = ownerItemsLastBookings.stream()
                .collect(Collectors.toMap(
                        ItemLastBookingProjection::getId,
                        ItemLastBookingProjection::getLastBooking,
                        (a, b) -> a));

        Map<Long, LocalDateTime> nextBookingByItemId = ownerItemsNextBookings.stream()
                .collect(Collectors.toMap(
                        ItemNextBookingProjection::getId,
                        ItemNextBookingProjection::getNextBooking,
                        (a, b) -> a));

        Map<Long, List<Comment>> commentsByItemId = ownerItemComments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        return ownerItems.stream()
                .map(item -> {
                    ItemGetAllResponseDto itemDto = ItemMapper
                            .mapItemToItemWithBookingDatesResponseDto(item);
                    itemDto.setLastBooking(lastBookingByItemId.get(item.getId()));
                    itemDto.setNextBooking(nextBookingByItemId.get(item.getId()));
                    itemDto.setComments(commentsByItemId.getOrDefault(item.getId(), List.of()).stream()
                            .map(CommentMapper::mapCommentToCommentResponseDto)
                            .toList());
                    return itemDto;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponseDto> search(String text) {
        if (text.isBlank()) return List.of();

        List<Item> userItems = itemRepository.findAllByText(text);
        return userItems.stream()
                .map(ItemMapper::mapItemToItemResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public void remove(Long id) {
        log.info("Delete item. ItemId={}", id);

        itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Вещь не найдена по id=%s".formatted(id)));

        itemRepository.deleteById(id);
        log.info("Item deleted. ItemId={}", id);
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long userId, Long itemId, CommentCreateRequestDto commentCreateRequestDto) {
        log.info("Add comment: commentDto: {}", commentCreateRequestDto);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден по id=%s".formatted(userId)));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Вещь не найдена по id=%s".formatted(itemId)));

        /* Проверка, что пользователь, который пишет комментарий, действительно брал вещь в аренду.
         * Отзыв может оставить только тот пользователь, который брал эту вещь в аренду, и только после
         * окончания срока аренды.
         * */
        List<Booking> userItemBookings = bookingRepository
                .findAllByBookerIdAndItemIdAndStatusAndEndBeforeOrderByEndDesc(userId,
                        itemId,
                        BookingStatus.APPROVED,
                        LocalDateTime.now());

        if (userItemBookings.isEmpty()) {
            throw new IllegalStateException("Невозможно создать комментарий: пользователь не брал вещь в аренду");
        }

        Comment comment = commentRepository.save(CommentMapper
                .mapCommentCreateRequestDtoToComment(commentCreateRequestDto, user, item));
        log.info("Comment added: {}", comment);
        return CommentMapper.mapCommentToCommentResponseDto(comment);
    }
}
