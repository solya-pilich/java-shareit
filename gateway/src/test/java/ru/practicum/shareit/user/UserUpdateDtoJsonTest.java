package ru.practicum.shareit.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
class UserUpdateDtoJsonTest {

    private final JacksonTester<UserUpdateDto> json;
    private final Validator validator;

    @Test
    void serialize_shouldWriteAllFields() throws Exception {
        UserUpdateDto dto = new UserUpdateDto("User1", "user1@mail.com");

        JsonContent<UserUpdateDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("User1");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("user1@mail.com");
    }

    @Test
    void serialize_shouldWriteNullFields() throws Exception {
        UserUpdateDto dto = new UserUpdateDto(null, null);

        JsonContent<UserUpdateDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isNull();
        assertThat(result).extractingJsonPathStringValue("$.email").isNull();
    }


    @Test
    void deserialize_shouldReadAllFields() throws Exception {
        String jsonString = "{\"name\":\"User1\",\"email\":\"user1@mail.com\"}";

        UserUpdateDto dto = json.parseObject(jsonString);

        assertThat(dto.getName()).isEqualTo("User1");
        assertThat(dto.getEmail()).isEqualTo("user1@mail.com");
    }

    @Test
    void deserialize_shouldAllowMissingFields() throws Exception {
        String jsonString = "{}";

        UserUpdateDto dto = json.parseObject(jsonString);

        assertThat(dto.getName()).isNull();
        assertThat(dto.getEmail()).isNull();
    }

    @Test
    void validate_shouldPass_whenBothFieldsValid() {
        UserUpdateDto dto = new UserUpdateDto("User1", "user1@mail.com");

        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_shouldPass_whenBothFieldsNull() {
        UserUpdateDto dto = new UserUpdateDto(null, null);

        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_shouldFail_whenEmailIsInvalid() {
        UserUpdateDto dto = new UserUpdateDto(null, "not-an-email");

        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
    }
}
