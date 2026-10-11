package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.testutil.TestDataFactory;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(TestDataFactory.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplTest {

    private final ItemRequestService itemRequestService;
    private final TestDataFactory factory;

    @Test
    void create_shouldSaveRequest() {
        User requestor = factory.createUser("User1", "user1@mail.com");
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Ищу дрель");

        ItemRequestDto created = itemRequestService.create(requestor.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Ищу дрель");
        assertThat(created.getCreated()).isNotNull();
        assertThat(created.getItems()).isEmpty();
    }

    @Test
    void create_shouldThrow_whenUserNotFound() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Ищу дрель");

        assertThatThrownBy(() -> itemRequestService.create(999L, dto))
                .isInstanceOf(NotFoundException.class);
    }



    @Test
    void getOwnRequests_shouldReturnRequests_withItems() {
        User requestor = factory.createUser("User1", "user1@mail.com");
        User owner = factory.createUser("Owner", "owner@mail.com");
        var request = factory.createItemRequest("Ищу дрель", requestor);
        factory.createItemWithRequest("Дрель", "Мощная", true, owner.getId(), request.getId());

        List<ItemRequestDto> result = new ArrayList<>(itemRequestService.getOwnRequests(requestor.getId()));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getItems()).hasSize(1);
        assertThat(result.getFirst().getItems().getFirst().getName()).isEqualTo("Дрель");
        assertThat(result.getFirst().getItems().getFirst().getOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    void getOwnRequests_shouldReturnOnlyOwnRequests() {
        User user1 = factory.createUser("User1", "user1@mail.com");
        User user2 = factory.createUser("User2", "user2@mail.com");
        factory.createItemRequest("Запрос User1", user1);
        factory.createItemRequest("Запрос User2", user2);

        List<ItemRequestDto> result = new ArrayList<>(itemRequestService.getOwnRequests(user1.getId()));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Запрос User1");
    }

    @Test
    void getOwnRequests_shouldReturnEmpty_whenNoRequests() {
        User requestor = factory.createUser("User1", "user1@mail.com");

        Collection<ItemRequestDto> result = itemRequestService.getOwnRequests(requestor.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void getOwnRequests_shouldThrow_whenUserNotFound() {
        assertThatThrownBy(() -> itemRequestService.getOwnRequests(999L))
                .isInstanceOf(NotFoundException.class);
    }



    @Test
    void getAllRequests_shouldReturnOtherUsersRequests() {
        User user1 = factory.createUser("User1", "user1@mail.com");
        User user2 = factory.createUser("User2", "user2@mail.com");
        factory.createItemRequest("Запрос User2", user2);

        List<ItemRequestDto> result = new ArrayList<>(itemRequestService.getAllRequests(user1.getId(), 0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Запрос User2");
    }

    @Test
    void getAllRequests_shouldNotReturnOwnRequests() {
        User user1 = factory.createUser("User1", "user1@mail.com");
        factory.createItemRequest("Свой запрос", user1);

        Collection<ItemRequestDto> result = itemRequestService.getAllRequests(user1.getId(), 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllRequests_shouldSupportPagination() {
        User requestor = factory.createUser("User1", "user1@mail.com");
        User viewer = factory.createUser("Viewer", "viewer@mail.com");
        factory.createItemRequest("Запрос 1", requestor);
        factory.createItemRequest("Запрос 2", requestor);
        factory.createItemRequest("Запрос 3", requestor);

        Collection<ItemRequestDto> firstPage = itemRequestService.getAllRequests(viewer.getId(), 0, 2);
        Collection<ItemRequestDto> secondPage = itemRequestService.getAllRequests(viewer.getId(), 2, 2);

        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(1);
    }

    @Test
    void getAllRequests_shouldReturnEmpty_whenNoOtherRequests() {
        User user = factory.createUser("User1", "user1@mail.com");

        Collection<ItemRequestDto> result = itemRequestService.getAllRequests(user.getId(), 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllRequests_shouldThrow_whenUserNotFound() {
        assertThatThrownBy(() -> itemRequestService.getAllRequests(999L, 0, 10))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllRequests_shouldThrow_whenInvalidPagination() {
        User user = factory.createUser("User1", "user1@mail.com");

        assertThatThrownBy(() -> itemRequestService.getAllRequests(user.getId(), -1, 10))
                .isInstanceOf(ValidationException.class);

        assertThatThrownBy(() -> itemRequestService.getAllRequests(user.getId(), 0, 0))
                .isInstanceOf(ValidationException.class);
    }




    @Test
    void getRequestById_shouldReturnRequest_withItems() {
        User requestor = factory.createUser("User1", "user1@mail.com");
        User owner = factory.createUser("Owner", "owner@mail.com");
        var request = factory.createItemRequest("Ищу дрель", requestor);
        factory.createItemWithRequest("Дрель", "Мощная", true, owner.getId(), request.getId());

        ItemRequestDto result = itemRequestService.getRequestById(owner.getId(), request.getId());

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().getFirst().getName()).isEqualTo("Дрель");
    }

    @Test
    void getRequestById_shouldReturnRequest_forAnyUser() {
        User requestor = factory.createUser("User1", "user1@mail.com");
        User other = factory.createUser("Other", "other@mail.com");
        var request = factory.createItemRequest("Ищу дрель", requestor);

        ItemRequestDto result = itemRequestService.getRequestById(other.getId(), request.getId());

        assertThat(result.getId()).isEqualTo(request.getId());
    }

    @Test
    void getRequestById_shouldThrow_whenRequestNotFound() {
        User user = factory.createUser("User1", "user1@mail.com");

        assertThatThrownBy(() -> itemRequestService.getRequestById(user.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getRequestById_shouldThrow_whenUserNotFound() {
        User requestor = factory.createUser("User1", "user1@mail.com");
        var request = factory.createItemRequest("Ищу дрель", requestor);

        assertThatThrownBy(() -> itemRequestService.getRequestById(999L, request.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}
