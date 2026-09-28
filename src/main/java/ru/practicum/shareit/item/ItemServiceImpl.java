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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemResponseDto addByUserId(Long userId, ItemCreateRequestDto itemCreateRequestDto) {
        log.info("Add item: itemDto: {}", itemCreateRequestDto);
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден по id=%s"
                        .formatted(userId)));

        Item item = ItemMapper.mapItemCreateRequestDtoToItem(itemCreateRequestDto);
        item.setOwnerId(userId);

        itemRepository.save(item);
        return ItemMapper.mapItemToItemResponseDto(item);
    }

    @Override
    @Transactional
    public ItemResponseDto updateById(Long userId, Long itemId, ItemUpdateRequestDto itemUpdateRequestDto) {

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
    public ItemGetByIdResponseDto get(Long id) {
        log.info("Get item. ItemId={}", id);
        Item item = itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Вещь не найдена по id=%s".formatted(id)));

        List<Comment> comments = commentRepository.findAllByItemId(id);

        return ItemMapper.mapItemToItemGetByIdResponseDto(item,
                comments.stream().map(CommentMapper::mapCommentToCommentResponseDto).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemGetAllResponseDto> getAllByUserId(Long ownerId) {
        log.info("Get user items. UserId={}", ownerId);
        List<Item> userItems = itemRepository.getAllByOwnerId(ownerId);

        List<ItemGetAllResponseDto> result = userItems.stream()
                .map(ItemMapper::mapItemToItemWithBookingDatesResponseDto)
                .toList();

        var now = LocalDateTime.now();
        return result.stream()
                .map(item -> {
                    item.setLastBooking(bookingRepository.getLastBookingEndDateByItemId(item.getId(), now));
                    item.setNextBooking(bookingRepository.getNextBookingStartDateByItemId(item.getId(), now));
                    List<Comment> comments = commentRepository.findAllByItemId(item.getId());
                    item.setComments(comments.stream().map(CommentMapper::mapCommentToCommentResponseDto).toList());
                    return item;
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
                .findAllPastByBookerIdAndItemIdAnStatus(userId,
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
