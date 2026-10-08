package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserUpdateRequestDtoJsonTest {
    @Autowired
    private JacksonTester<UserUpdateRequestDto> json;

    @Test
    void deserialize_partialJson_leavesOtherFieldsNull() throws Exception {
        UserUpdateRequestDto dto = json.parseObject("{\"name\":\"Jane\"}");

        assertThat(dto.getName()).isEqualTo("Jane");
        assertThat(dto.getEmail()).isNull();
    }

    @Test
    void deserialize_emptyJson_givesAllNull() throws Exception {
        UserUpdateRequestDto dto = json.parseObject("{}");

        assertThat(dto.getName()).isNull();
        assertThat(dto.getEmail()).isNull();
    }

    @Test
    void serialize_exposesAtLeastOneFieldProvidedProperty() throws Exception {
        UserUpdateRequestDto dto = new UserUpdateRequestDto();
        dto.setEmail("jane@mail.com");

        assertThat(json.write(dto)).hasJsonPathBooleanValue("$.atLeastOneFieldProvided");
        assertThat(json.write(dto)).extractingJsonPathBooleanValue("$.atLeastOneFieldProvided").isEqualTo(true);
    }

    @Test
    void roundTrip_emailOnly() throws Exception {
        UserUpdateRequestDto dto = new UserUpdateRequestDto();
        dto.setEmail("jane@mail.com");

        UserUpdateRequestDto parsed = json.parseObject(json.write(dto).getJson());

        assertThat(parsed.getEmail()).isEqualTo("jane@mail.com");
        assertThat(parsed.getName()).isNull();
    }
}
