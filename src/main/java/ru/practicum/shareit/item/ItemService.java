package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    ItemResponseDto addByUserId(Long userId, ItemCreateRequestDto itemCreateRequestDto);

    ItemResponseDto updateById(Long userId, Long itemId, ItemUpdateRequestDto itemUpdateRequestDto);

    ItemGetByIdResponseDto get(Long userId, Long id);

    List<ItemGetAllResponseDto> getAllByOwnerId(Long userId);

    List<ItemResponseDto> search(String text);

    void remove(Long id);

    CommentResponseDto addComment(Long userId, Long itemId, CommentCreateRequestDto commentCreateRequestDto);
}
