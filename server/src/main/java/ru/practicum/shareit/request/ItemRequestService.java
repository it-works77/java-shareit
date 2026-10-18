package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestCreateResponseDto addByUserId(Long requestorId, ItemRequestRequestDto itemRequestRequestDto);

    ItemRequestResponseDto getById(Long userId, Long requestId);

    List<ItemRequestResponseDto> getAllByRequestorId(Long requestorId);

    List<ItemRequestResponseDto> getAllNotMy(Long requestorId);

}
