package alertbus.user_service.controller;

import alertbus.user_service.dto.request.LoginRequestDTO;
import alertbus.user_service.dto.request.UserRequestDTO;
import alertbus.user_service.dto.response.UserResponseDTO;
import alertbus.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


//Testa apenas o mapeamento HTTP do controller (sem Spring Security).Regras de autorização ficam no SecurityConfig e devem ser cobertas por teste de integração.

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void postUsers_deveRetornar201ESemExporSenha() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.createUser(any(UserRequestDTO.class)))
                .thenReturn(new UserResponseDTO(id, "Ana", "ana@alertbus.com"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"ana@alertbus.com\",\"password\":\"123456\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("ana@alertbus.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        ArgumentCaptor<UserRequestDTO> captor = ArgumentCaptor.forClass(UserRequestDTO.class);
        verify(userService).createUser(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().name()).isEqualTo("Ana");
    }

    @Test
    void getPorId_deveRetornar200() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getUserById(id)).thenReturn(new UserResponseDTO(id, "Ana", "ana@alertbus.com"));

        mockMvc.perform(get("/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana"));
    }

    @Test
    void getPorId_comUuidInvalido_deveRetornar400() throws Exception {
        mockMvc.perform(get("/users/nao-e-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void getUsers_deveRetornar200ComLista() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                new UserResponseDTO(UUID.randomUUID(), "Ana", "ana@alertbus.com"),
                new UserResponseDTO(UUID.randomUUID(), "João", "joao@alertbus.com")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void login_deveRetornar200ComToken() throws Exception {
        when(userService.Login(any(LoginRequestDTO.class))).thenReturn("jwt.token.aqui");

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@alertbus.com\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.aqui"));
    }
}
