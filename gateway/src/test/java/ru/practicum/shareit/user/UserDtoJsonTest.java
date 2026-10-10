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
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
public class UserDtoJsonTest {

    private final JacksonTester<UserDto> json;
    private final Validator validator;

    @Test
    void serialize_shouldWriteAllFields() throws Exception {
        UserDto dto = new UserDto(1L, "User1", "user1@mail.com");

        JsonContent<UserDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("User1");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("user1@mail.com");
    }

    @Test
    void serialize_shouldWriteNullFields() throws Exception {
        UserDto dto = new UserDto(null, null, null);

        JsonContent<UserDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isNull();
        assertThat(result).extractingJsonPathStringValue("$.name").isNull();
        assertThat(result).extractingJsonPathStringValue("$.email").isNull();
    }

    @Test
    void deserialize_shouldReadAllFields() throws Exception {
        String jsonString = "{\"id\":1,\"name\":\"User1\",\"email\":\"user1@mail.com\"}";

        UserDto dto = json.parseObject(jsonString);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("User1");
        assertThat(dto.getEmail()).isEqualTo("user1@mail.com");
    }

    @Test
    void deserialize_shouldAllowMissingFields() throws Exception {
        String jsonString = "{}";

        UserDto dto = json.parseObject(jsonString);

        assertThat(dto.getId()).isNull();
        assertThat(dto.getName()).isNull();
        assertThat(dto.getEmail()).isNull();
    }

    @Test
    void validate_shouldPass_whenAllFieldsValid() {
        UserDto dto = new UserDto(1L, "User1", "user1@mail.com");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_shouldFail_whenNameIsBlank() {
        UserDto dto = new UserDto(1L, "", "user1@mail.com");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    void validate_shouldFail_whenNameIsNull() {
        UserDto dto = new UserDto(1L, null, "user1@mail.com");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    void validate_shouldFail_whenEmailIsInvalid() {
        UserDto dto = new UserDto(1L, "User1", "bad-email");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    void validate_shouldFail_whenEmailIsBlank() {
        UserDto dto = new UserDto(1L, "User1", "");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    void validate_shouldFail_whenBothNameAndEmailAreInvalid() {
        UserDto dto = new UserDto(1L, "", "bad-email");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(2);
    }
}
