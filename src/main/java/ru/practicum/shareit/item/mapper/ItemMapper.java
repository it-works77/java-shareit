package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {
    public static Item mapItemCreateRequestDtoToItem(ItemCreateRequestDto itemCreateRequestDto) {
        return Item.builder()
                .name(itemCreateRequestDto.getName())
                .description(itemCreateRequestDto.getDescription())
                .available(itemCreateRequestDto.getAvailable())
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

    public static ItemGetByIdResponseDto mapItemToItemGetByIdResponseDto(Item item,
                                                                  List<CommentResponseDto> commentResponseDtos) {
        return ItemGetByIdResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(commentResponseDtos)
                .build();
    }

    public static ItemGetAllResponseDto mapItemToItemWithBookingDatesResponseDto(Item item) {
        return ItemGetAllResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }
}
