package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserCreateRequestDtoJsonTest {
    @Autowired
    private JacksonTester<UserCreateRequestDto> json;

    @Test
    void serialize_usesFieldNames() throws Exception {
        UserCreateRequestDto dto = new UserCreateRequestDto();
        dto.setName("John");
        dto.setEmail("john@mail.com");

        assertThat(json.write(dto)).extractingJsonPathStringValue("$.name").isEqualTo("John");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.email").isEqualTo("john@mail.com");
    }

    @Test
    void roundTrip_keepsFields() throws Exception {
        UserCreateRequestDto dto = new UserCreateRequestDto();
        dto.setName("John");
        dto.setEmail("john@mail.com");

        UserCreateRequestDto parsed = json.parseObject(json.write(dto).getJson());

        assertThat(parsed.getName()).isEqualTo("John");
        assertThat(parsed.getEmail()).isEqualTo("john@mail.com");
    }
}
