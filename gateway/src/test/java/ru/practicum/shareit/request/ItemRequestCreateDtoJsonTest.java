package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestCreateDtoJsonTest {

    private final JacksonTester<ItemRequestCreateDto> json;
    private final Validator validator;

    @Test
    void serialize_shouldWriteDescription() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Ищу дрель");

        JsonContent<ItemRequestCreateDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Ищу дрель");
    }

    @Test
    void serialize_shouldWriteNullDescription() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto(null);

        JsonContent<ItemRequestCreateDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.description").isNull();
    }

    @Test
    void deserialize_shouldReadDescription() throws Exception {
        String jsonString = "{\"description\":\"Ищу дрель\"}";

        ItemRequestCreateDto dto = json.parseObject(jsonString);

        assertThat(dto.getDescription()).isEqualTo("Ищу дрель");
    }

    @Test
    void deserialize_shouldAllowMissingDescription() throws Exception {
        String jsonString = "{}";

        ItemRequestCreateDto dto = json.parseObject(jsonString);

        assertThat(dto.getDescription()).isNull();
    }

    @Test
    void validate_shouldPass_whenDescriptionValid() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Ищу дрель");

        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_shouldFail_whenDescriptionIsBlank() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("");

        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("description");
    }

    @Test
    void validate_shouldFail_whenDescriptionIsNull() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto(null);

        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("description");
    }
}
