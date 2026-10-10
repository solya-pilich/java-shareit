package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    @Autowired
    private MockMvc mvc;

    @Test
    void add_shouldReturnCreatedItem() throws Exception {
        ItemDto request = new ItemDto("Дрель", "Мощная", true, null);
        TestItemResponse response = new TestItemResponse(1L, "Дрель", "Мощная", true);

        when(itemClient.add(eq(1L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.description").value(response.getDescription()))
                .andExpect(jsonPath("$.available").value(response.getAvailable()));
    }

    @Test
    void add_shouldReturn400_whenNameIsBlank() throws Exception {
        ItemDto request = new ItemDto("", "Мощная", true, null);

        mvc.perform(post("/items")
                .header(USER_ID_HEADER, 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_shouldReturn400_whenDescriptionIsBlank() throws Exception {
        ItemDto request = new ItemDto("Дрель", "", true, null);

        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_shouldReturn400_whenAvailableIsNull() throws Exception {
        ItemDto request = new ItemDto("Дрель", "Мощная", null, null);

        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }



    @Test
    void update_shouldReturnUpdatedItem() throws Exception {
        ItemUpdateDto request = new ItemUpdateDto("Дрель Updated", null, null);
        TestItemResponse response = new TestItemResponse(1L, "Дрель Updated", "Мощная", true);

        when(itemClient.update(eq(1L), eq(1L), any(ItemUpdateDto.class)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.description").value(response.getDescription()));
    }

    @Test
    void update_shouldReturn200_whenOnlyAvailableChanged() throws Exception {
        ItemUpdateDto request = new ItemUpdateDto(null, null, false);
        TestItemResponse response = new TestItemResponse(1L, "Дрель", "Мощная", false);

        when(itemClient.update(eq(1L), eq(1L), any(ItemUpdateDto.class)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }



    @Test
    void getById_shouldReturnItem() throws Exception {
        TestItemResponse response = new TestItemResponse(1L, "Дрель", "Мощная", true);

        when(itemClient.getById(eq(1L), eq(1L)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(get("/items/1")
                        .header(USER_ID_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.name").value(response.getName()));
    }



    @Test
    void getAll_shouldReturnItemsList() throws Exception {
        TestItemResponse item1 = new TestItemResponse(1L, "Дрель", "Мощная", true);
        TestItemResponse item2 = new TestItemResponse(2L, "Пила", "Острая", true);

        when(itemClient.getAll(1L))
                .thenReturn(ResponseEntity.ok(List.of(item1, item2)));

        mvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(item1.getId()))
                .andExpect(jsonPath("$[0].name").value(item1.getName()))
                .andExpect(jsonPath("$[1].id").value(item2.getId()))
                .andExpect(jsonPath("$[1].name").value(item2.getName()));
    }

    @Test
    void getAll_shouldReturnEmptyList() throws Exception {
        when(itemClient.getAll(1L))
                .thenReturn(ResponseEntity.ok(List.of()));

        mvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }



    @Test
    void search_shouldReturnMatchingItems() throws Exception {
        TestItemResponse item = new TestItemResponse(1L, "Дрель аккумуляторная", "Мощная", true);

        when(itemClient.search(eq(1L), eq("дрель")))
                .thenReturn(ResponseEntity.ok(List.of(item)));

        mvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1)
                        .param("text", "дрель")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value(item.getName()));
    }

    @Test
    void search_shouldReturnEmptyList() throws Exception {
        when(itemClient.search(eq(1L), eq("несуществующая")))
                .thenReturn(ResponseEntity.ok(List.of()));

        mvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1)
                        .param("text", "несуществующая")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void search_shouldReturn400_whenTextIsMissing() throws Exception {
        mvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }



    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        CommentCreateDto request = new CommentCreateDto("Отличная дрель!");
        TestCommentResponse response = new TestCommentResponse(1L, "Отличная дрель!", "User1");

        when(itemClient.addComment(eq(1L), eq(1L), any(CommentCreateDto.class)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.text").value(response.getText()))
                .andExpect(jsonPath("$.authorName").value(response.getAuthorName()));
    }

    @Test
    void addComment_shouldReturn400_whenTextIsBlank() throws Exception {
        CommentCreateDto request = new CommentCreateDto("");

        mvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
