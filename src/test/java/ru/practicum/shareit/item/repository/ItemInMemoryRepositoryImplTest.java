package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ItemInMemoryRepositoryImplTest {

    private ItemInMemoryRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new ItemInMemoryRepositoryImpl();
    }

    @Test
    void add_whenNewItem_returnsItemWithGeneratedId() {
        Item item = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();

        Item saved = repository.add(item);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId());
        assertEquals("Drill", saved.getName());
        assertEquals("Power drill", saved.getDescription());
        assertTrue(saved.getAvailable());
        assertEquals(1L, saved.getOwnerId());
    }

    @Test
    void add_multipleItems_incrementsId() {
        Item item1 = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();
        Item item2 = Item.builder().name("Saw").description("Hand saw").available(true).ownerId(1L).build();
        Item item3 = Item.builder().name("Hammer").description("Claw hammer").available(true).ownerId(1L).build();

        repository.add(item1);
        repository.add(item2);
        Item saved3 = repository.add(item3);

        assertEquals(3L, saved3.getId());
    }

    @Test
    void update_whenIdIsNull_throwsIllegalArgumentException() {
        Item item = Item.builder().id(null).name("Drill").description("Power drill").available(true).ownerId(1L).build();

        assertThrows(IllegalArgumentException.class, () -> repository.update(item));
    }

    @Test
    void update_whenItemNotFound_throwsEntityNotFoundException() {
        Item item = Item.builder().id(999L).name("Drill").description("Power drill").available(true).ownerId(1L).build();

        assertThrows(EntityNotFoundException.class, () -> repository.update(item));
    }

    @Test
    void update_whenFound_updatesOnlyProvidedFields() {
        Item item = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();
        Item saved = repository.add(item);

        Item update = Item.builder().id(saved.getId()).name("Saw").build();
        Item updated = repository.update(update);

        assertEquals(saved.getId(), updated.getId());
        assertEquals("Saw", updated.getName());
        assertEquals("Power drill", updated.getDescription());
        assertTrue(updated.getAvailable());
        assertEquals(1L, updated.getOwnerId());
    }

    @Test
    void update_whenOnlyDescriptionProvided_updatesDescriptionOnly() {
        Item item = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();
        Item saved = repository.add(item);

        Item update = Item.builder().id(saved.getId()).description("Cordless drill").build();
        Item updated = repository.update(update);

        assertEquals("Drill", updated.getName());
        assertEquals("Cordless drill", updated.getDescription());
        assertTrue(updated.getAvailable());
    }

    @Test
    void update_whenOnlyAvailableProvided_updatesAvailableOnly() {
        Item item = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();
        Item saved = repository.add(item);

        Item update = Item.builder().id(saved.getId()).available(false).build();
        Item updated = repository.update(update);

        assertEquals("Drill", updated.getName());
        assertEquals("Power drill", updated.getDescription());
        assertFalse(updated.getAvailable());
    }

    @Test
    void get_whenExists_returnsItem() {
        Item item = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();
        Item saved = repository.add(item);

        Optional<Item> found = repository.get(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Drill", found.get().getName());
        assertEquals("Power drill", found.get().getDescription());
        assertTrue(found.get().getAvailable());
        assertEquals(1L, found.get().getOwnerId());
    }

    @Test
    void get_whenNotExists_returnsEmpty() {
        Optional<Item> found = repository.get(999L);

        assertTrue(found.isEmpty());
    }

    @Test
    void getAll_whenEmpty_returnsEmptyList() {
        List<Item> all = repository.getAll();

        assertTrue(all.isEmpty());
    }

    @Test
    void getAll_whenHasItems_returnsAllItems() {
        Item item1 = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();
        Item item2 = Item.builder().name("Saw").description("Hand saw").available(true).ownerId(1L).build();
        repository.add(item1);
        repository.add(item2);

        List<Item> all = repository.getAll();

        assertEquals(2, all.size());
    }

    @Test
    void getAllByUserId_whenEmpty_returnsEmptyList() {
        List<Item> items = repository.getAllByUserId(1L);

        assertTrue(items.isEmpty());
    }

    @Test
    void getAllByUserId_whenHasItems_returnsOnlyOwnedItems() {
        Item item1 = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();
        Item item2 = Item.builder().name("Saw").description("Hand saw").available(true).ownerId(2L).build();
        Item item3 = Item.builder().name("Hammer").description("Claw hammer").available(true).ownerId(1L).build();
        repository.add(item1);
        repository.add(item2);
        repository.add(item3);

        List<Item> user1Items = repository.getAllByUserId(1L);

        assertEquals(2, user1Items.size());
        assertTrue(user1Items.stream().allMatch(i -> i.getOwnerId().equals(1L)));
    }

    @Test
    void search_caseInsensitiveByName() {
        Item item1 = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();
        Item item2 = Item.builder().name("Saw").description("Hand saw").available(true).ownerId(1L).build();
        repository.add(item1);
        repository.add(item2);

        List<Item> found = repository.search(1L, "drill");

        assertEquals(1, found.size());
        assertEquals("Drill", found.getFirst().getName());
    }

    @Test
    void search_caseInsensitiveByDescription() {
        Item item1 = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();
        Item item2 = Item.builder().name("Saw").description("Hand saw").available(true).ownerId(1L).build();
        repository.add(item1);
        repository.add(item2);

        List<Item> found = repository.search(1L, "POWER");

        assertEquals(1, found.size());
        assertEquals("Drill", found.getFirst().getName());
    }

    @Test
    void search_filtersByOwnerId() {
        Item item1 = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();
        Item item2 = Item.builder().name("Drill").description("Cordless drill").available(true).ownerId(2L).build();
        repository.add(item1);
        repository.add(item2);

        List<Item> found = repository.search(1L, "drill");

        assertEquals(1, found.size());
        assertEquals(1L, found.getFirst().getOwnerId());
    }

    @Test
    void search_filtersOnlyAvailable() {
        Item item1 = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();
        Item item2 = Item.builder().name("Saw").description("Hand saw").available(false).ownerId(1L).build();
        repository.add(item1);
        repository.add(item2);

        List<Item> found = repository.search(1L, "drill");

        assertEquals(1, found.size());
        assertTrue(found.getFirst().getAvailable());
    }

    @Test
    void search_whenEmptyText_returnsAllAvailableOwnedItems() {
        Item item1 = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();
        Item item2 = Item.builder().name("Saw").description("Hand saw").available(true).ownerId(1L).build();
        Item item3 = Item.builder().name("Hammer").description("Claw hammer").available(false).ownerId(1L).build();
        repository.add(item1);
        repository.add(item2);
        repository.add(item3);

        List<Item> found = repository.search(1L, "");

        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(Item::getAvailable));
    }

    @Test
    void search_whenNoMatches_returnsEmptyList() {
        Item item1 = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();
        repository.add(item1);

        List<Item> found = repository.search(1L, "hammer");

        assertTrue(found.isEmpty());
    }

    @Test
    void search_whenNameIsNull_throwsIllegalArgumentException() {
        Item item = Item.builder().description("Power drill").available(true).ownerId(1L).build();
        repository.add(item);

        assertThrows(IllegalArgumentException.class, () -> repository.search(1L, "drill"));
    }

    @Test
    void search_whenDescriptionIsNull_throwsIllegalArgumentException() {
        Item item = Item.builder().name("Drill").available(true).ownerId(1L).build();
        repository.add(item);

        assertThrows(IllegalArgumentException.class, () -> repository.search(1L, "drill"));
    }

    @Test
    void search_whenBothNameAndDescriptionAreNull_throwsIllegalArgumentException() {
        Item item = Item.builder().available(true).ownerId(1L).build();
        repository.add(item);

        assertThrows(IllegalArgumentException.class, () -> repository.search(1L, "drill"));
    }

    @Test
    void remove_whenExists_returnsTrueAndRemovesItem() {
        Item item = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();
        Item saved = repository.add(item);

        boolean removed = repository.remove(saved.getId());

        assertTrue(removed);
        assertTrue(repository.get(saved.getId()).isEmpty());
    }

    @Test
    void remove_whenNotExists_returnsFalse() {
        boolean removed = repository.remove(999L);

        assertFalse(removed);
    }

    @Test
    void remove_thenAdd_generatesNewId() {
        Item item = Item.builder().name("Drill").description("Power drill").available(true).ownerId(1L).build();
        Item saved = repository.add(item);
        repository.remove(saved.getId());

        Item newItem = Item.builder().name("Saw").description("Hand saw").available(true).ownerId(1L).build();
        Item newSaved = repository.add(newItem);

        assertEquals(2L, newSaved.getId());
    }
}