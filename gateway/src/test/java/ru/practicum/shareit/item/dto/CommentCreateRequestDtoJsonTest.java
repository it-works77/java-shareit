package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentCreateRequestDtoJsonTest {
    @Autowired
    private JacksonTester<CommentCreateRequestDto> json;

    @Test
    void serialize_usesTextField() throws Exception {
        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        dto.setText("Great drill");

        assertThat(json.write(dto)).extractingJsonPathStringValue("$.text").isEqualTo("Great drill");
    }

    @Test
    void roundTrip_keepsText() throws Exception {
        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        dto.setText("Great drill");

        CommentCreateRequestDto parsed = json.parseObject(json.write(dto).getJson());

        assertThat(parsed.getText()).isEqualTo("Great drill");
    }
}
