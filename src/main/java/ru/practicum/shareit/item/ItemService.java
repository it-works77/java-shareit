package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemUpdateRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {
    ItemResponseDto addByUserId(Long userId, ItemCreateRequestDto itemCreateRequestDto);

    ItemResponseDto updateById(Long id, ItemUpdateRequestDto itemUpdateRequestDto);

    ItemResponseDto get(Long id);

    List<ItemResponseDto> getAllByUserId(Long userId);

    List<ItemResponseDto> search(Long userId, String text);

    void remove(Long id);
}
