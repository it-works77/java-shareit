package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemUpdateRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemResponseDto addByUserId(Long userId, ItemCreateRequestDto itemCreateRequestDto) {
        log.info("Add item: itemDto: {}", itemCreateRequestDto);
        Item item = ItemMapper.mapItemCreateRequestDtoToItem(itemCreateRequestDto);
        item.setOwnerId(userId);

        // If user doesn't exists then throws EntityNotFoundException as required
        userService.get(userId);

        itemRepository.add(item);
        return ItemMapper.mapItemToItemResponseDto(item);
    }

    @Override
    public ItemResponseDto updateById(Long userId, Long itemId, ItemUpdateRequestDto itemUpdateRequestDto) {
        Item existingItem = itemRepository.get(itemId).orElseThrow(
                () -> new EntityNotFoundException("Вещь не найдена по id=%s".formatted(itemId)));

        if (!Objects.equals(userId, existingItem.getOwnerId())) {
            throw new EntityNotFoundException("У пользователя userId=%s нет вещи с id=%s"
                    .formatted(userId, itemId));
        }

        Item newItem = ItemMapper.mapItemUpdateRequestDtoToItem(itemUpdateRequestDto);
        newItem.setId(itemId);

        Item updatedItem = itemRepository.update(newItem);
        return ItemMapper.mapItemToItemResponseDto(updatedItem);
    }

    @Override
    public ItemResponseDto get(Long id) {
        log.info("Get item. ItemId = %d".formatted(id));
        Item item = itemRepository.get(id).orElseThrow(
                () -> new EntityNotFoundException("Вещь не найдена по id=%s".formatted(id)));
        return ItemMapper.mapItemToItemResponseDto(item);
    }

    @Override
    public List<ItemResponseDto> getAllByUserId(Long userId) {
        log.info("Get user items. UserId = %d".formatted(userId));
        List<Item> userItems = itemRepository.getAllByUserId(userId);
        return userItems.stream()
                .map(ItemMapper::mapItemToItemResponseDto)
                .toList();
    }

    @Override
    public List<ItemResponseDto> search(Long userId, String text) {
        if (text.isBlank()) return List.of();

        List<Item> userItems = itemRepository.search(userId, text);
        return userItems.stream()
                .map(ItemMapper::mapItemToItemResponseDto)
                .toList();
    }

    @Override
    public void remove(Long id) {
        log.info("Delete item. ItemId = %d".formatted(id));
        if (itemRepository.remove(id)) {
            log.info("Item deleted. ItemId = %d".formatted(id));
        } else {
            throw new EntityNotFoundException("Вещь не найдена по id=%s".formatted(id));
        }
    }
}
