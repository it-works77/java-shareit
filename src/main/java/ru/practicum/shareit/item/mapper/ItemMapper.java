package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemCreateRequestDto;
import ru.practicum.shareit.item.dto.ItemUpdateRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {
    public static Item mapItemCreateRequestDtoToItem(ItemCreateRequestDto itemCreateRequestDto) {
        return Item.builder()
                .name(itemCreateRequestDto.getName())
                .description(itemCreateRequestDto.getDescription())
                .available(itemCreateRequestDto.getAvailable())
                .build();
    }

    public static Item mapItemUpdateRequestDtoToItem(ItemUpdateRequestDto itemUpdateRequestDto) {
        return Item.builder()
                .name(itemUpdateRequestDto.getName())
                .description(itemUpdateRequestDto.getDescription())
                .available(itemUpdateRequestDto.getAvailable())
                .build();
    }

    public static ItemResponseDto mapItemToItemResponseDto(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }
}
