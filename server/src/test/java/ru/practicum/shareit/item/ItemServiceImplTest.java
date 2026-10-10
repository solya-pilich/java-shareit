package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.testutil.TestDataFactory;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestDataFactory.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplTest {

    private final ItemService itemService;
    private final TestDataFactory factory;

    @Test
    void add_shouldSaveItem_withoutRequest() {
        User owner = factory.createUser("User1", "user1@mail.com");
        ItemDto dto = new ItemDto(null, "Дрель", "Мощная", true, null, null);

        ItemDto created = itemService.add(owner.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Дрель");
        assertThat(created.getAvailable()).isTrue();
    }

    @Test
    void add_shouldSaveItem_withRequest() {
        User requestor = factory.createUser("Bob", "bob@mail.com");
        User owner = factory.createUser("User1", "user1@mail.com");

        ItemRequest request = factory.createItemRequest("Ищу дрель", requestor);

        ItemDto dto = new ItemDto(null, "Дрель", "Мощная", true, null, request.getId());

        ItemDto created = itemService.add(owner.getId(), dto);

        assertThat(created.getId()).isNotNull();
        Item savedItem = factory.findItemById(created.getId());
        assertThat(savedItem.getRequest()).isEqualTo(request.getId());
    }

    @Test
    void add_shouldThrow_whenUserNotFound() {
        ItemDto dto = new ItemDto(null, "Дрель", "Мощная", true, null, null);

        assertThatThrownBy(() -> itemService.add(999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void add_shouldThrow_whenRequestNotFound() {
        User owner = factory.createUser("User1", "user1@mail.com");
        ItemDto dto = new ItemDto(null, "Дрель", "Мощная", true, null, 999L);

        assertThatThrownBy(() -> itemService.add(owner.getId(), dto))
                .isInstanceOf(NotFoundException.class);
    }



    @Test
    void update_shouldChangeName() {
        User owner = factory.createUser("User1", "user1@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Дрель Updated");

        ItemDto updated = itemService.update(owner.getId(), item.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Дрель Updated");
        assertThat(updated.getDescription()).isEqualTo("Мощная");
    }

    @Test
    void update_shouldChangeAvailable() {
        User owner = factory.createUser("User1", "user1@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setAvailable(false);

        ItemDto updated = itemService.update(owner.getId(), item.getId(), updateDto);

        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void update_shouldThrow_whenNotOwner() {
        User owner = factory.createUser("User1", "user1@mail.com");
        User other = factory.createUser("OtherUser", "otherUser@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Пила");

        assertThatThrownBy(() -> itemService.update(other.getId(), item.getId(), updateDto))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void update_shouldThrow_whenItemNotFound() {
        User owner = factory.createUser("User1", "user1@mail.com");
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("X");

        assertThatThrownBy(() -> itemService.update(owner.getId(), 999L, updateDto))
                .isInstanceOf(NotFoundException.class);
    }



    @Test
    void findById_shouldReturnItem_withoutBookings() {
        User owner = factory.createUser("User1", "user1@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        ItemWithBookingsDto dto = itemService.findById(item.getId(), owner.getId());

        assertThat(dto.getId()).isEqualTo(item.getId());
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
    }

    @Test
    void findById_shouldReturnItem_withLastAndNextBooking_forOwner() {
        User owner = factory.createUser("User1", "user1@mail.com");
        User booker = factory.createUser("Bob", "bob@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item, booker, now.minusDays(3), now.minusDays(2), BookingStatus.APPROVED);
        factory.createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED);

        ItemWithBookingsDto dto = itemService.findById(item.getId(), owner.getId());

        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getNextBooking()).isNotNull();
    }

    @Test
    void findById_shouldNotFillBookings_forNonOwner() {
        User owner = factory.createUser("User1", "user1@mail.com");
        User other = factory.createUser("Bob", "bob@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item, other, now.minusDays(3), now.minusDays(2), BookingStatus.APPROVED);

        ItemWithBookingsDto dto = itemService.findById(item.getId(), other.getId());

        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
    }

    @Test
    void findById_shouldReturnComments() {
        User owner = factory.createUser("User1", "user1@mail.com");
        User booker = factory.createUser("Bob", "bob@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());
        factory.createComment(item, booker, "Отличная дрель!");

        ItemWithBookingsDto dto = itemService.findById(item.getId(), owner.getId());

        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().getFirst().getText()).isEqualTo("Отличная дрель!");
    }

    @Test
    void findById_shouldThrow_whenItemNotFound() {
        User owner = factory.createUser("User1", "user1@mail.com");

        assertThatThrownBy(() -> itemService.findById(999L, owner.getId()))
                .isInstanceOf(NotFoundException.class);
    }



    @Test
    void findByOwnerId_shouldReturnItems() {
        User owner = factory.createUser("User1", "user1@mail.com");
        factory.createItem("Дрель", "Мощная", true, owner.getId());
        factory.createItem("Пила", "Острая", true, owner.getId());

        Collection<ItemWithBookingsDto> items = itemService.findByOwnerId(owner.getId());

        assertThat(items).hasSize(2);
        assertThat(items).extracting(ItemWithBookingsDto::getName)
                .containsExactlyInAnyOrder("Дрель", "Пила");
    }

    @Test
    void findByOwnerId_shouldReturnEmpty_whenNoItems() {
        User owner = factory.createUser("User1", "user1@mail.com");

        Collection<ItemWithBookingsDto> items = itemService.findByOwnerId(owner.getId());

        assertThat(items).isEmpty();
    }

    @Test
    void findByOwnerId_shouldThrow_whenUserNotFound() {
        assertThatThrownBy(() -> itemService.findByOwnerId(999L))
                .isInstanceOf(NotFoundException.class);
    }



    @Test
    void findByText_shouldFindByName() {
        User owner = factory.createUser("User1", "user1@mail.com");
        factory.createItem("Дрель аккумуляторная", "Мощная", true, owner.getId());
        factory.createItem("Пила", "Острая", true, owner.getId());

        Collection<ItemDto> found = itemService.findByText(owner.getId(), "дрель");

        assertThat(found).hasSize(1);
        assertThat(found).extracting(ItemDto::getName)
                .containsExactly("Дрель аккумуляторная");
    }

    @Test
    void findByText_shouldFindByDescription() {
        User owner = factory.createUser("User1", "user1@mail.com");
        factory.createItem("Дрель", "Мощная аккумуляторная", true, owner.getId());

        Collection<ItemDto> found = itemService.findByText(owner.getId(), "аккумуляторная");

        assertThat(found).hasSize(1);
        assertThat(found).extracting(ItemDto::getDescription)
                .containsExactly("Мощная аккумуляторная");
    }

    @Test
    void findByText_shouldReturnEmpty_forBlankText() {
        User owner = factory.createUser("User1", "user1@mail.com");
        factory.createItem("Дрель", "Мощная", true, owner.getId());

        assertThat(itemService.findByText(owner.getId(), "")).isEmpty();
        assertThat(itemService.findByText(owner.getId(), "   ")).isEmpty();
    }

    @Test
    void findByText_shouldIgnoreUnavailableItems() {
        User owner = factory.createUser("User1", "user1@mail.com");
        factory.createItem("Дрель", "Мощная", false, owner.getId());

        Collection<ItemDto> found = itemService.findByText(owner.getId(), "дрель");

        assertThat(found).isEmpty();
    }

    @Test
    void findByText_shouldThrow_whenUserNotFound() {
        assertThatThrownBy(() -> itemService.findByText(999L, "дрель"))
                .isInstanceOf(NotFoundException.class);
    }



    @Test
    void addComment_shouldSaveComment_whenBookingFinished() {
        User owner = factory.createUser("User1", "user1@mail.com");
        User booker = factory.createUser("Bob", "bob@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item, booker, now.minusDays(3), now.minusDays(2), BookingStatus.APPROVED);

        CommentCreateDto dto = new CommentCreateDto("Отличная дрель!");

        CommentDto saved = itemService.addComment(booker.getId(), item.getId(), dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getText()).isEqualTo("Отличная дрель!");
        assertThat(saved.getAuthorName()).isEqualTo("Bob");
    }

    @Test
    void addComment_shouldThrow_whenNoApprovedBooking() {
        User owner = factory.createUser("User1", "user1@mail.com");
        User booker = factory.createUser("Bob", "bob@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        CommentCreateDto dto = new CommentCreateDto("Отличная дрель!");

        assertThatThrownBy(() -> itemService.addComment(booker.getId(), item.getId(), dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void addComment_shouldThrow_whenBookingNotFinished() {
        User owner = factory.createUser("User1", "user1@mail.com");
        User booker = factory.createUser("Bob", "bob@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        LocalDateTime now = LocalDateTime.now();
        factory.createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED);

        CommentCreateDto dto = new CommentCreateDto("Отличная дрель!");

        assertThatThrownBy(() -> itemService.addComment(booker.getId(), item.getId(), dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void addComment_shouldThrow_whenUserNotFound() {
        User owner = factory.createUser("User1", "user1@mail.com");
        Item item = factory.createItem("Дрель", "Мощная", true, owner.getId());

        CommentCreateDto dto = new CommentCreateDto("Отличная дрель!");

        assertThatThrownBy(() -> itemService.addComment(999L, item.getId(), dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addComment_shouldThrow_whenItemNotFound() {
        User booker = factory.createUser("Bob", "bob@mail.com");
        CommentCreateDto dto = new CommentCreateDto("Отличная дрель!");

        assertThatThrownBy(() -> itemService.addComment(booker.getId(), 999L, dto))
                .isInstanceOf(NotFoundException.class);
    }
}
