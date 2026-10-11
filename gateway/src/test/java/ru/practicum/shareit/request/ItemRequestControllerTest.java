package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestClient requestClient;

    @Autowired
    private MockMvc mvc;

    private TestItemRequestResponse buildRequest(Long id, String description) {
        return new TestItemRequestResponse(
                id,
                description,
                LocalDateTime.now(),
                List.of()
        );
    }

    @Test
    void create_shouldReturnCreatedRequest() throws Exception {
        ItemRequestCreateDto request = new ItemRequestCreateDto("Ищу дрель");
        TestItemRequestResponse response = buildRequest(1L, "Ищу дрель");

        when(requestClient.create(eq(1L), any(ItemRequestCreateDto.class)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.description").value(response.getDescription()))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void create_shouldReturn400_whenDescriptionIsBlank() throws Exception {
        ItemRequestCreateDto request = new ItemRequestCreateDto("");

        mvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenDescriptionIsNull() throws Exception {
        ItemRequestCreateDto request = new ItemRequestCreateDto(null);

        mvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }



    @Test
    void getOwnRequests_shouldReturnRequestsList() throws Exception {
        TestItemRequestResponse r1 = buildRequest(1L, "Ищу дрель");
        TestItemRequestResponse r2 = buildRequest(2L, "Ищу пилу");

        when(requestClient.getOwnRequests(1L))
                .thenReturn(ResponseEntity.ok(List.of(r1, r2)));

        mvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(r1.getId()))
                .andExpect(jsonPath("$[0].description").value(r1.getDescription()))
                .andExpect(jsonPath("$[1].id").value(r2.getId()));
    }

    @Test
    void getOwnRequests_shouldReturnEmptyList() throws Exception {
        when(requestClient.getOwnRequests(1L))
                .thenReturn(ResponseEntity.ok(List.of()));

        mvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }



    @Test
    void getAllRequests_shouldReturnRequestsList() throws Exception {
        TestItemRequestResponse r1 = buildRequest(1L, "Ищу дрель");

        when(requestClient.getAllRequests(eq(1L), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok(List.of(r1)));

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(r1.getId()));
    }

    @Test
    void getAllRequests_shouldUseProvidedPagination() throws Exception {
        TestItemRequestResponse r1 = buildRequest(1L, "Ищу дрель");

        when(requestClient.getAllRequests(eq(1L), eq(2), eq(5)))
                .thenReturn(ResponseEntity.ok(List.of(r1)));

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1)
                        .param("from", "2")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllRequests_shouldReturnEmptyList() throws Exception {
        when(requestClient.getAllRequests(eq(1L), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok(List.of()));

        mvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }



    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        TestItemShortForRequest item = new TestItemShortForRequest(1L, "Дрель", 2L);
        TestItemRequestResponse response = new TestItemRequestResponse(
                1L,
                "Ищу дрель",
                LocalDateTime.now(),
                List.of(item)
        );

        when(requestClient.getRequestById(eq(1L), eq(1L)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.description").value(response.getDescription()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(item.getId()))
                .andExpect(jsonPath("$.items[0].name").value(item.getName()))
                .andExpect(jsonPath("$.items[0].ownerId").value(item.getOwnerId()));
    }

    @Test
    void getRequestById_shouldReturnRequestWithoutItems() throws Exception {
        TestItemRequestResponse response = buildRequest(1L, "Ищу дрель");

        when(requestClient.getRequestById(eq(1L), eq(1L)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0));
    }
}
