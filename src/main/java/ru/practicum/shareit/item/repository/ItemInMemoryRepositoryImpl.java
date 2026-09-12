package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class ItemInMemoryRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private Long currentItemId = 1L;

    @Override
    public Item add(Item item) {
        item.setId(getId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {

        Item existingItem = items.get(item.getId());
        if (existingItem == null) {
            throw new EntityNotFoundException("Вещь не найдена по id=%s".formatted(item.getId()));
        }

        Optional.ofNullable(item.getName()).ifPresent(existingItem::setName);
        Optional.ofNullable(item.getDescription()).ifPresent(existingItem::setDescription);
        Optional.ofNullable(item.getAvailable()).ifPresent(existingItem::setAvailable);

        return existingItem;
    }

    @Override
    public Optional<Item> get(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> getAll() {
        return items.values().stream()
                .toList();
    }

    @Override
    public List<Item> getAllByUserId(Long userId) {

        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(userId))
                .toList();
    }

    @Override
    public List<Item> search(Long userId, String text) {
        String textInLowerCase = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Objects.equals(userId, item.getOwnerId()))
                .filter(item -> {
                    if (item.getName() == null || item.getDescription() == null) {
                        throw new IllegalArgumentException("name и description не должны быть null");
                    }
                    return item.getName().toLowerCase().contains(textInLowerCase)
                            || item.getDescription().toLowerCase().contains(textInLowerCase);
                })
                .filter(Item::getAvailable)
                .toList();
    }


    @Override
    public boolean remove(Long id) {
        return items.remove(id) != null;
    }

    private Long getId() {
        return currentItemId++;
    }
}
