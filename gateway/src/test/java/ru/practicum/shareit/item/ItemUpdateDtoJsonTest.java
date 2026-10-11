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
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemUpdateDtoJsonTest {

    private final JacksonTester<ItemUpdateDto> json;
    private final Validator validator;

    @Test
    void serialize_shouldWriteAllFields() throws Exception {
        ItemUpdateDto dto = new ItemUpdateDto("Дрель", "Мощная", true);

        JsonContent<ItemUpdateDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Мощная");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
    }

    @Test
    void deserialize_shouldReadAllFields() throws Exception {
        String jsonString = "{\"name\":\"Дрель\",\"description\":\"Мощная\",\"available\":true}";

        ItemUpdateDto dto = json.parseObject(jsonString);

        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Мощная");
        assertThat(dto.getAvailable()).isTrue();
    }

    @Test
    void deserialize_shouldAllowMissingFields() throws Exception {
        String jsonString = "{}";

        ItemUpdateDto dto = json.parseObject(jsonString);

        assertThat(dto.getName()).isNull();
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getAvailable()).isNull();
    }

    @Test
    void validate_shouldPass_whenAllFieldsValid() {
        ItemUpdateDto dto = new ItemUpdateDto("Дрель", "Мощная", true);

        Set<ConstraintViolation<ItemUpdateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_shouldPass_whenAllFieldsNull() {
        ItemUpdateDto dto = new ItemUpdateDto(null, null, null);

        Set<ConstraintViolation<ItemUpdateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_shouldFail_whenNameIsEmpty() {
        ItemUpdateDto dto = new ItemUpdateDto("", null, null);

        Set<ConstraintViolation<ItemUpdateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    void validate_shouldFail_whenDescriptionIsEmpty() {
        ItemUpdateDto dto = new ItemUpdateDto(null, "", null);

        Set<ConstraintViolation<ItemUpdateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("description");
    }

    @Test
    void validate_shouldPass_whenOnlyAvailableChanged() {
        ItemUpdateDto dto = new ItemUpdateDto(null, null, false);

        Set<ConstraintViolation<ItemUpdateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
