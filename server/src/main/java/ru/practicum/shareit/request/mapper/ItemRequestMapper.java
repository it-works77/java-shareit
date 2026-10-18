package ru.practicum.shareit.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.RequestItemResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {
    public static ItemRequest toItemRequest(ItemRequestRequestDto itemRequestRequestDto, User requester) {
        return ItemRequest.builder()
                .description(itemRequestRequestDto.getDescription())
                .requestor(requester)
                .created(LocalDateTime.now())
                .build();
    }

    public static ItemRequestCreateResponseDto toItemRequestCreateResponseDto(ItemRequest itemRequest) {
        return ItemRequestCreateResponseDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequestResponseDto toItemRequestResponseDto(ItemRequest itemRequest,
                                                                  List<RequestItemResponseDto> itemResponseDtoList) {
        return ItemRequestResponseDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(itemResponseDtoList)
                .build();
    }
}
