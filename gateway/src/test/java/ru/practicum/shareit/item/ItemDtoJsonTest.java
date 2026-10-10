package ru.practicum.shareit.item;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemDtoJsonTest {

    private final JacksonTester<ItemDto> json;
    private final Validator validator;

    @Test
    void serialize_shouldWriteAllFields() throws Exception {
        ItemDto dto = new ItemDto("Дрель", "Мощная", true, 5L);

        JsonContent<ItemDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Мощная");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(5);
    }

    @Test
    void serialize_shouldWriteNullRequestId() throws Exception {
        ItemDto dto = new ItemDto("Дрель", "Мощная", true, null);

        JsonContent<ItemDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.requestId").isNull();
    }

    @Test
    void deserialize_shouldReadAllFields() throws Exception {
        String jsonString = "{\"name\":\"Дрель\",\"description\":\"Мощная\",\"available\":true,\"requestId\":5}";

        ItemDto dto = json.parseObject(jsonString);

        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Мощная");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(5L);
    }

    @Test
    void deserialize_shouldAllowMissingRequestId() throws Exception {
        String jsonString = "{\"name\":\"Дрель\",\"description\":\"Мощная\",\"available\":true}";

        ItemDto dto = json.parseObject(jsonString);

        assertThat(dto.getRequestId()).isNull();
    }

    @Test
    void validate_shouldPass_whenAllFieldsValid() {
        ItemDto dto = new ItemDto("Дрель", "Мощная", true, null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_shouldFail_whenNameIsBlank() {
        ItemDto dto = new ItemDto("", "Мощная", true, null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    void validate_shouldFail_whenDescriptionIsBlank() {
        ItemDto dto = new ItemDto("Дрель", "", true, null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("description");
    }

    @Test
    void validate_shouldFail_whenAvailableIsNull() {
        ItemDto dto = new ItemDto("Дрель", "Мощная", null, null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("available");
    }

    @Test
    void validate_shouldFail_whenMultipleFieldsInvalid() {
        ItemDto dto = new ItemDto("", "", null, null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(3);
    }
}
