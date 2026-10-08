package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemModelTest {
    @Test
    void of_copiesFieldsWithoutRequestId() {
        Item source = Item.builder()
                .id(1L)
                .name("Drill")
                .description("d")
                .available(true)
                .ownerId(2L)
                .requestId(77L)
                .build();

        Item copy = Item.of(source);

        assertEquals(1L, copy.getId());
        assertEquals("Drill", copy.getName());
        assertEquals("d", copy.getDescription());
        assertEquals(Boolean.TRUE, copy.getAvailable());
        assertEquals(2L, copy.getOwnerId());
        assertNull(copy.getRequestId());
    }
}
