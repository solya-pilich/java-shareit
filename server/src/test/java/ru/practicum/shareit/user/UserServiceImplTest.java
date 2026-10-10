package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.exception.DuplicateDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.testutil.TestDataFactory;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(TestDataFactory.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {

    private final UserService userService;
    private final TestDataFactory factory;

    @Test
    void create_shouldSaveUser() {
        UserDto dto = new UserDto(null, "User1", "user1@mail.com");

        UserDto created = userService.create(dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("User1");
        assertThat(created.getEmail()).isEqualTo("user1@mail.com");
    }

    @Test
    void create_shouldThrow_whenEmailDuplicated() {
        factory.createUser("User1", "user1@mail.com");
        UserDto duplicate = new UserDto(null, "OtherUser", "user1@mail.com");

        assertThatThrownBy(() -> userService.create(duplicate))
                .isInstanceOf(DuplicateDataException.class);
    }



    @Test
    void findById_shouldReturnUser() {
        User user = factory.createUser("User1", "user1@mail.com");

        UserDto found = userService.findById(user.getId());

        assertThat(found.getId()).isEqualTo(user.getId());
        assertThat(found.getName()).isEqualTo("User1");
        assertThat(found.getEmail()).isEqualTo("user1@mail.com");
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(NotFoundException.class);
    }



    @Test
    void findAll_shouldReturnAllUsers() {
        factory.createUser("User1", "user1@mail.com");
        factory.createUser("User2", "user2@mail.com");

        Collection<UserDto> users = userService.findAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(UserDto::getEmail)
                .containsExactlyInAnyOrder("user1@mail.com", "user2@mail.com");
    }

    @Test
    void findAll_shouldReturnEmpty_whenNoUsers() {
        Collection<UserDto> users = userService.findAll();

        assertThat(users).isEmpty();
    }



    @Test
    void update_shouldChangeBothFields() {
        User user = factory.createUser("User1", "old@mail.com");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("Bob");
        updateDto.setEmail("new@mail.com");

        UserDto updated = userService.update(updateDto, user.getId());

        assertThat(updated.getId()).isEqualTo(user.getId());
        assertThat(updated.getName()).isEqualTo("Bob");
        assertThat(updated.getEmail()).isEqualTo("new@mail.com");
    }

    @Test
    void update_shouldChangeOnlyName() {
        User user = factory.createUser("User1", "user1@mail.com");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("Bob");

        UserDto updated = userService.update(updateDto, user.getId());

        assertThat(updated.getName()).isEqualTo("Bob");
        assertThat(updated.getEmail()).isEqualTo("user1@mail.com");
    }

    @Test
    void update_shouldChangeOnlyEmail() {
        User user = factory.createUser("User1", "old@mail.com");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("new@mail.com");

        UserDto updated = userService.update(updateDto, user.getId());

        assertThat(updated.getName()).isEqualTo("User1");
        assertThat(updated.getEmail()).isEqualTo("new@mail.com");
    }

    @Test
    void update_shouldThrow_whenUserNotFound() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("User1");

        assertThatThrownBy(() -> userService.update(updateDto, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_shouldThrow_whenEmailDuplicated() {
        factory.createUser("User1", "user1@mail.com");
        User otherUser = factory.createUser("OtherUser", "otheruser@mail.com");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("user1@mail.com");

        assertThatThrownBy(() -> userService.update(updateDto, otherUser.getId()))
                .isInstanceOf(DuplicateDataException.class);
    }



    @Test
    void delete_shouldRemoveUser() {
        User user = factory.createUser("User1", "user1@mail.com");

        userService.delete(user.getId());

        assertThatThrownBy(() -> userService.findById(user.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_shouldThrow_whenUserNotFound() {
        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
