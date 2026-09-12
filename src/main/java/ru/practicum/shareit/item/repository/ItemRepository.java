package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item add(Item item);

    Item update(Item item);

    Optional<Item> get(Long id);

    List<Item> getAll();

    List<Item> getAllByUserId(Long userId);

    List<Item> search(Long userId, String text);

    boolean remove(Long id);
}
