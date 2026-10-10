package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private MockMvc mvc;

    private TestBookingResponse buildBookingResponse(Long id, String status) {
        return new TestBookingResponse(
                id,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                status,
                new TestUserShort(2L, "Booker"),
                new TestItemShort(1L, "Дрель", 1L)
        );
    }

    @Test
    void create_shouldReturnCreatedBooking() throws Exception {
        BookItemRequestDto request = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        TestBookingResponse response = buildBookingResponse(1L, "WAITING");

        when(bookingClient.create(eq(1L), any(BookItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.item.id").value(response.getItem().getId()))
                .andExpect(jsonPath("$.item.name").value(response.getItem().getName()))
                .andExpect(jsonPath("$.booker.id").value(response.getBooker().getId()));
    }

    @Test
    void create_shouldReturn400_whenItemIdIsNull() throws Exception {
        BookItemRequestDto request = new BookItemRequestDto(
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenStartIsNull() throws Exception {
        BookItemRequestDto request = new BookItemRequestDto(
                1L,
                null,
                LocalDateTime.now().plusDays(2)
        );

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenEndIsNull() throws Exception {
        BookItemRequestDto request = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                null
        );

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenStartInPast() throws Exception {
        BookItemRequestDto request = new BookItemRequestDto(
                1L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }



    @Test
    void approve_shouldReturnApprovedBooking() throws Exception {
        TestBookingResponse response = buildBookingResponse(1L, "APPROVED");

        when(bookingClient.approve(eq(1L), eq(1L), eq(true)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approve_shouldReturnRejectedBooking() throws Exception {
        TestBookingResponse response = buildBookingResponse(1L, "REJECTED");

        when(bookingClient.approve(eq(1L), eq(1L), eq(false)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void approve_shouldReturn400_whenApprovedMissing() throws Exception {
        mvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isBadRequest());
    }



    @Test
    void getById_shouldReturnBooking() throws Exception {
        TestBookingResponse response = buildBookingResponse(1L, "WAITING");

        when(bookingClient.getById(eq(1L), eq(1L)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }



    @Test
    void getByBooker_shouldUseDefaultState() throws Exception {
        TestBookingResponse response = buildBookingResponse(1L, "WAITING");

        when(bookingClient.getByBooker(eq(1L), eq(BookingState.ALL)))
                .thenReturn(ResponseEntity.ok(List.of(response)));

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(response.getId()));
    }

    @Test
    void getByBooker_shouldUseProvidedState() throws Exception {
        TestBookingResponse response = buildBookingResponse(1L, "APPROVED");

        when(bookingClient.getByBooker(eq(1L), eq(BookingState.CURRENT)))
                .thenReturn(ResponseEntity.ok(List.of(response)));

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .param("state", "CURRENT")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByBooker_shouldBeCaseInsensitive() throws Exception {
        TestBookingResponse response = buildBookingResponse(1L, "APPROVED");

        when(bookingClient.getByBooker(eq(1L), eq(BookingState.CURRENT)))
                .thenReturn(ResponseEntity.ok(List.of(response)));

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .param("state", "current")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getByBooker_shouldReturn400_whenStateUnknown() throws Exception {
        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .param("state", "UNKNOWN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByBooker_shouldReturnEmptyList() throws Exception {
        when(bookingClient.getByBooker(eq(1L), eq(BookingState.ALL)))
                .thenReturn(ResponseEntity.ok(List.of()));

        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }



    @Test
    void getByOwner_shouldUseDefaultState() throws Exception {
        TestBookingResponse response = buildBookingResponse(1L, "WAITING");

        when(bookingClient.getByOwner(eq(1L), eq(BookingState.ALL)))
                .thenReturn(ResponseEntity.ok(List.of(response)));

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByOwner_shouldUseProvidedState() throws Exception {
        TestBookingResponse response = buildBookingResponse(1L, "APPROVED");

        when(bookingClient.getByOwner(eq(1L), eq(BookingState.PAST)))
                .thenReturn(ResponseEntity.ok(List.of(response)));

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1)
                        .param("state", "PAST")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByOwner_shouldReturn400_whenStateUnknown() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1)
                        .param("state", "UNKNOWN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
