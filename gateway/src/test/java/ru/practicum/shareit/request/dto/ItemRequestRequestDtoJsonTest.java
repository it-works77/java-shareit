package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestRequestDto> json;

    @Test
    void serialize_usesDescriptionField() throws Exception {
        ItemRequestRequestDto dto = new ItemRequestRequestDto();
        dto.setDescription("Need a drill");

        assertThat(json.write(dto)).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");
    }

    @Test
    void roundTrip_keepsDescription() throws Exception {
        ItemRequestRequestDto dto = new ItemRequestRequestDto();
        dto.setDescription("Need a drill");

        ItemRequestRequestDto parsed = json.parseObject(json.write(dto).getJson());

        assertThat(parsed.getDescription()).isEqualTo("Need a drill");
    }
}
