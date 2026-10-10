package ru.practicum.shareit.testutil;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@TestComponent
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TestDataFactory {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    public User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    public Item createItem(String name, String description, boolean available, Long ownerId) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(ownerId);
        return itemRepository.save(item);
    }

    public Booking createBooking(Item item, User booker,
                                 LocalDateTime start, LocalDateTime end,
                                 BookingStatus status) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    public Comment createComment(Item item, User author, String text) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    public ItemRequest createItemRequest(String description, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        return itemRequestRepository.save(request);
    }

    public Item findItemById(Long id) {
        return itemRepository.findById(id).orElseThrow();
    }

    public Item createItemWithRequest(String name, String description, boolean available,
                                      Long ownerId, Long requestId) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(ownerId);
        item.setRequest(requestId);
        return itemRepository.save(item);
    }
}
