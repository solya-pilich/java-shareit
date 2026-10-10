package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient userClient;

    @Autowired
    private MockMvc mvc;

    @Test
    void create_shouldReturnCreatedUser() throws Exception {
        UserDto request = new UserDto(null, "User1", "user1@mail.com");
        UserDto response = new UserDto(1L, "User1", "user1@mail.com");

        when(userClient.create(any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));
    }

    @Test
    void create_shouldReturn400_whenNameIsBlank() throws Exception {
        UserDto request = new UserDto(null, "", "user1@mail.com");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenEmailIsBlank() throws Exception {
        UserDto request = new UserDto(null, "User1", "");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenEmailIsInvalid() throws Exception {
        UserDto request = new UserDto(null, "User1", "bad_email");

        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mapper)))
                .andExpect(status().isBadRequest());
    }



    @Test
    void update_shouldReturnUpdatedUser() throws Exception {
        UserUpdateDto request = new UserUpdateDto("User1 Updated", "updated@mail.com");
        UserDto response = new UserDto(1L, "User1 Updated", "updated@mail.com");

        when(userClient.update(eq(1L), any(UserUpdateDto.class)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));
    }

    @Test
    void update_shouldReturn200_whenOnlyNameChanged() throws Exception {
        UserUpdateDto request = new UserUpdateDto("User1 Updated", null);
        UserDto response = new UserDto(1L, "User1 Updated", "user1@mail.com");

        when(userClient.update(eq(1L), any(UserUpdateDto.class)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("User1 Updated"))
                .andExpect(jsonPath("$.email").value("user1@mail.com"));
    }

    @Test
    void update_shouldReturn400_whenEmailIsInvalid() throws Exception {
        UserUpdateDto request = new UserUpdateDto(null, "bad_email");

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }



    @Test
    void findById_shouldReturnUser() throws Exception {
        UserDto response = new UserDto(1L, "User1", "user1@mail.com");

        when(userClient.findById(1L))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(get("/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.email").value(response.getEmail()));
    }

    @Test
    void findAll_shouldReturnUsersList() throws Exception {
        List<UserDto> response = List.of(
                new UserDto(1L, "User1", "user1@mail.com"),
                new UserDto(2L, "User2", "user2@mail.com")
        );

        when(userClient.findAll())
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("User1"))
                .andExpect(jsonPath("$[0].email").value("user1@mail.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("User2"))
                .andExpect(jsonPath("$[1].email").value("user2@mail.com"));
    }

    @Test
    void findAll_shouldReturnEmptyList() throws Exception {
        when(userClient.findAll())
                .thenReturn(ResponseEntity.ok(List.of()));

        mvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }



    @Test
    void delete_shouldReturnOk() throws Exception {
        when(userClient.delete(1L))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

}
