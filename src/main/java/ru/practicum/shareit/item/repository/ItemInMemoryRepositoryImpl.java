package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

@Repository
public class ItemInMemoryRepositoryImpl implements ItemRepository {
    @Override
    public Item add(Item item) {
        return null;
    }

    @Override
    public Item update(Item item) {
        return null;
    }

    @Override
    public Optional<Item> get(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Item> getAll() {
        return List.of();
    }

    @Override
    public boolean remove(Integer id) {
        return false;
    }
}
