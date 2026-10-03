package alertbus.user_service.service;

import alertbus.user_service.domain.entity.User;
import alertbus.user_service.domain.entity.UserRole;
import alertbus.user_service.dto.request.LoginRequestDTO;
import alertbus.user_service.dto.request.UserRequestDTO;
import alertbus.user_service.dto.response.UserResponseDTO;
import alertbus.user_service.repository.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserService userService;

    private User usuario(UUID id) {
        return User.builder()
                .id(id)
                .name("Ana")
                .email("ana@alertbus.com")
                .password("HASH")
                .role(UserRole.PASSENGER)
                .build();
    }

    private void stubSaveComId(UUID id) {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(id);
            return u;
        });
    }


    @Test
    void createUser_comEmailNovo_deveCodificarSenhaESalvar() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsByEmail("ana@alertbus.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("HASH");
        stubSaveComId(id);

        UserResponseDTO response = userService.createUser(
                new UserRequestDTO("Ana", "ana@alertbus.com", "123456", null));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User salvo = captor.getValue();
        assertThat(salvo.getName()).isEqualTo("Ana");
        assertThat(salvo.getEmail()).isEqualTo("ana@alertbus.com");
        assertThat(salvo.getPassword()).isEqualTo("HASH").isNotEqualTo("123456");

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Ana");
        assertThat(response.email()).isEqualTo("ana@alertbus.com");
    }

    @Test
    void createUser_semRole_deveAssumirPassenger() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("HASH");
        stubSaveComId(UUID.randomUUID());

        userService.createUser(new UserRequestDTO("Ana", "ana@alertbus.com", "123456", null));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.PASSENGER);
    }

    @Test
    void createUser_comRoleInformada_deveRespeitarARole() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("HASH");
        stubSaveComId(UUID.randomUUID());

        userService.createUser(new UserRequestDTO("João", "joao@alertbus.com", "123456", UserRole.DRIVER));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.DRIVER);
    }

    @Test
    void createUser_comEmailDuplicado_deveLancarExcecaoENaoSalvar() {
        when(userRepository.existsByEmail("ana@alertbus.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(
                new UserRequestDTO("Ana", "ana@alertbus.com", "123456", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("E-mail já cadastrado");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }


    @Test
    void getUserById_existente_deveRetornarDTOSemSenha() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(usuario(id)));

        UserResponseDTO result = userService.getUserById(id);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Ana");
        assertThat(result.email()).isEqualTo("ana@alertbus.com");
    }

    @Test
    void getUserById_inexistente_deveLancarExcecao() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    void getAllUsers_deveMapearTodos() {
        when(userRepository.findAll()).thenReturn(List.of(usuario(UUID.randomUUID()), usuario(UUID.randomUUID())));

        assertThat(userService.getAllUsers()).hasSize(2);
    }

    @Test
    void getAllUsers_semUsuarios_deveRetornarListaVazia() {
        when(userRepository.findAll()).thenReturn(List.of());

        assertThat(userService.getAllUsers()).isEmpty();
    }



    @Test
    void login_comCredenciaisCorretas_deveRetornarTokenDoTokenService() {
        User user = usuario(UUID.randomUUID());
        when(userRepository.findByEmail("ana@alertbus.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "HASH")).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("jwt.token.aqui");

        String token = userService.Login(new LoginRequestDTO("ana@alertbus.com", "123456"));

        assertThat(token).isEqualTo("jwt.token.aqui");
    }

    @Test
    void login_comEmailInexistente_deveLancarExcecaoSemGerarToken() {
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.Login(new LoginRequestDTO("x@x.com", "123456")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuário ou senha inválid");

        verifyNoInteractions(passwordEncoder, tokenService);
    }

    @Test
    void login_comSenhaErrada_deveLancarExcecaoSemGerarToken() {
        User user = usuario(UUID.randomUUID());
        when(userRepository.findByEmail("ana@alertbus.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("errada", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> userService.Login(new LoginRequestDTO("ana@alertbus.com", "errada")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuário ou senha inválid");

        verify(tokenService, never()).generateToken(any());
    }


}
