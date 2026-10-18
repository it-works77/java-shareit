package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemCreateRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemCreateRequestDto> json;

    private static ItemCreateRequestDto dto(String name, String description, Boolean available, Long requestId) {
        ItemCreateRequestDto dto = new ItemCreateRequestDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        dto.setRequestId(requestId);
        return dto;
    }

    @Test
    void serialize_availableFalseIsNotNull() throws Exception {
        ItemCreateRequestDto dto = dto("Drill", "Power drill", false, null);

        assertThat(json.write(dto)).extractingJsonPathBooleanValue("$.available").isEqualTo(false);
    }

    @Test
    void serialize_availableNullStaysNull() throws Exception {
        ItemCreateRequestDto dto = dto("Drill", "Power drill", null, null);

        assertThat(json.write(dto)).extractingJsonPathValue("$.available").isNull();
    }

    @Test
    void deserialize_withoutRequestId_givesNull() throws Exception {
        String content = "{\"name\":\"Drill\",\"description\":\"Power drill\",\"available\":true}";

        ItemCreateRequestDto dto = json.parseObject(content);

        assertThat(dto.getRequestId()).isNull();
        assertThat(dto.getAvailable()).isTrue();
    }

    @Test
    void deserialize_withRequestId_keepsValue() throws Exception {
        String content = "{\"name\":\"Drill\",\"description\":\"Power drill\","
                + "\"available\":true,\"requestId\":3}";

        ItemCreateRequestDto dto = json.parseObject(content);

        assertThat(dto.getRequestId()).isEqualTo(3L);
    }

    @Test
    void roundTrip_keepsAllFields() throws Exception {
        ItemCreateRequestDto dto = dto("Drill", "Power drill", true, 3L);

        ItemCreateRequestDto parsed = json.parseObject(json.write(dto).getJson());

        assertThat(parsed.getName()).isEqualTo("Drill");
        assertThat(parsed.getDescription()).isEqualTo("Power drill");
        assertThat(parsed.getAvailable()).isTrue();
        assertThat(parsed.getRequestId()).isEqualTo(3L);
    }
}
