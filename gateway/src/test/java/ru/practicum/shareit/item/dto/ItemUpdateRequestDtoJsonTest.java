package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemUpdateRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemUpdateRequestDto> json;

    @Test
    void deserialize_partialJson_leavesOtherFieldsNull() throws Exception {
        ItemUpdateRequestDto dto = json.parseObject("{\"name\":\"Saw\"}");

        assertThat(dto.getName()).isEqualTo("Saw");
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getAvailable()).isNull();
    }

    @Test
    void deserialize_emptyJson_givesAllNull() throws Exception {
        ItemUpdateRequestDto dto = json.parseObject("{}");

        assertThat(dto.getName()).isNull();
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getAvailable()).isNull();
    }

    @Test
    void roundTrip_availableFlag() throws Exception {
        ItemUpdateRequestDto dto = new ItemUpdateRequestDto();
        dto.setAvailable(false);

        ItemUpdateRequestDto parsed = json.parseObject(json.write(dto).getJson());

        assertThat(parsed.getAvailable()).isFalse();
        assertThat(parsed.getName()).isNull();
    }

    @Test
    void serialize_exposesOnlyContractFields() throws Exception {
        ItemUpdateRequestDto dto = new ItemUpdateRequestDto();
        dto.setName("Вещь");
        dto.setDescription("Описание");
        dto.setAvailable(true);

        assertThat(json.write(dto)).hasJsonPathStringValue("$.name");
        assertThat(json.write(dto)).hasJsonPathStringValue("$.description");
        assertThat(json.write(dto)).hasJsonPathBooleanValue("$.available");
        assertThat(json.write(dto)).doesNotHaveJsonPath("$.atLeastOneFieldProvided");
    }
}
