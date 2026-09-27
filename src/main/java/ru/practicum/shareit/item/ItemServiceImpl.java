package ru.practicum.shareit.item;

import org.springframework.transaction.annotation.Transactional;
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

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ItemResponseDto addByUserId(Long userId, ItemCreateRequestDto itemCreateRequestDto) {
        log.info("Add item: itemDto: {}", itemCreateRequestDto);
        // If user doesn't exist then throws EntityNotFoundException as required
        userService.get(userId);

        Item item = ItemMapper.mapItemCreateRequestDtoToItem(itemCreateRequestDto);
        item.setOwnerId(userId);

        itemRepository.save(item);
        return ItemMapper.mapItemToItemResponseDto(item);
    }

    @Override
    @Transactional
    public ItemResponseDto updateById(Long userId, Long itemId, ItemUpdateRequestDto itemUpdateRequestDto) {
        if (userId == null || itemId == null) {
            throw new IllegalArgumentException("Аргументы должны быть заданы: userId=%s, itemId=%s"
                    .formatted(userId, itemId));
        }

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
    public ItemResponseDto get(Long id) {
        log.info("Get item. ItemId={}", id);
        Item item = itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Вещь не найдена по id=%s".formatted(id)));
        return ItemMapper.mapItemToItemResponseDto(item);
    }

    @Override
    public List<ItemResponseDto> getAllByUserId(Long ownerId) {
        log.info("Get user items. UserId={}", ownerId);
        List<Item> userItems = itemRepository.getAllByOwnerId(ownerId);
        return userItems.stream()
                .map(ItemMapper::mapItemToItemResponseDto)
                .toList();
    }

    @Override
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
}
