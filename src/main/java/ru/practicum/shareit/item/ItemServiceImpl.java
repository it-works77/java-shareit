package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    @Override
    public ItemResponseDto addByUserId(Long userId, ItemRequestDto itemRequestDto) {
        return null;
    }

    @Override
    public ItemResponseDto updateById(Long id, ItemRequestDto itemRequestDto) {
        return null;
    }

    @Override
    public ItemResponseDto get(Long id) {
        return null;
    }

    @Override
    public List<ItemResponseDto> getAllByUserId(Long userId) {
        return List.of();
    }

    @Override
    public List<ItemResponseDto> search(Long userId, String text) {
        return List.of();
    }

    @Override
    public boolean remove(Long id) {
        return false;
    }
}
