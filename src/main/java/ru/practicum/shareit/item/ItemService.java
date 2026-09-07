package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {
    ItemResponseDto addByUserId(Long userId, ItemRequestDto itemRequestDto);

    ItemResponseDto updateById(Long id, ItemRequestDto itemRequestDto);

    ItemResponseDto get(Long id);

    List<ItemResponseDto> getAllByUserId(Long userId);

    List<ItemResponseDto> search(Long userId, String text);

    boolean remove(Long id);
}
