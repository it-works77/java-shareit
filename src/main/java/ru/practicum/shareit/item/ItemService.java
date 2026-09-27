package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemUpdateRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDatesResponseDto;

import java.util.List;

public interface ItemService {
    ItemResponseDto addByUserId(Long userId, ItemCreateRequestDto itemCreateRequestDto);

    ItemResponseDto updateById(Long userId, Long itemId, ItemUpdateRequestDto itemUpdateRequestDto);

    ItemResponseDto get(Long id);

    List<ItemWithBookingDatesResponseDto> getAllByUserId(Long userId);

    List<ItemResponseDto> search(String text);

    void remove(Long id);
}
