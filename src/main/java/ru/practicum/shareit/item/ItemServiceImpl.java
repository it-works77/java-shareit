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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    @Override
    public ItemResponseDto addByUserId(Long userId, ItemCreateRequestDto itemCreateRequestDto) {
        log.info("Add item: itemDto: {}", itemCreateRequestDto);
        Item item = ItemMapper.mapItemCreateRequestDtoToItem(itemCreateRequestDto);
        item.setOwnerId(userId);
        itemRepository.add(item);
        return ItemMapper.mapItemToItemResponseDto(item);
    }

    @Override
    public ItemResponseDto updateById(Long id, ItemUpdateRequestDto itemUpdateRequestDto) {
        itemRepository.get(id).orElseThrow(
                () -> new EntityNotFoundException("Вещь не найдена по id=%s".formatted(id)));
        Item item = ItemMapper.mapItemUpdateRequestDtoToItem(itemUpdateRequestDto);
        item.setId(id);
        itemRepository.update(item);
        return ItemMapper.mapItemToItemResponseDto(item);
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
