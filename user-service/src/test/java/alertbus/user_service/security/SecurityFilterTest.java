package alertbus.user_service.security;

import alertbus.user_service.domain.entity.User;
import alertbus.user_service.domain.entity.UserRole;
import alertbus.user_service.repository.UserRepository;
import alertbus.user_service.service.TokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private TokenService tokenService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityFilter securityFilter;

    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final MockFilterChain chain = new MockFilterChain();

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private User usuario(UserRole role) {
        return User.builder()
                .id(UUID.randomUUID())
                .name("Ana")
                .email("ana@alertbus.com")
                .password("HASH")
                .role(role)
                .build();
    }

    @Test
    void semHeaderAuthorization_naoDeveAutenticarEDeveSeguirACadeia() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        securityFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(tokenService, userRepository);
    }

    @Test
    void headerSemPrefixoBearer_naoDeveAutenticar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");

        securityFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(tokenService, userRepository);
    }

    @Test
    void tokenInvalido_naoDeveAutenticarEDeveSeguirACadeia() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-ruim");
        when(tokenService.validateToken("token-ruim")).thenReturn("");

        securityFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(userRepository);
    }

    @Test
    void tokenValidoMasUsuarioInexistente_naoDeveAutenticar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-bom");
        when(tokenService.validateToken("token-bom")).thenReturn("fantasma@alertbus.com");
        when(userRepository.findByEmail("fantasma@alertbus.com")).thenReturn(Optional.empty());

        securityFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void tokenValidoEUsuarioExistente_deveAutenticarComAuthorityDaRole() throws Exception {
        User admin = usuario(UserRole.ADMIN_COMPANY);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-bom");
        when(tokenService.validateToken("token-bom")).thenReturn("ana@alertbus.com");
        when(userRepository.findByEmail("ana@alertbus.com")).thenReturn(Optional.of(admin));

        securityFilter.doFilter(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.isAuthenticated()).isTrue();
        assertThat(auth.getPrincipal()).isSameAs(admin);
        assertThat(auth.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN_COMPANY");
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void cadaRoleDeveGerarSuaPropriaAuthority() throws Exception {
        for (UserRole role : UserRole.values()) {
            SecurityContextHolder.clearContext();
            User user = usuario(role);
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer t-" + role.name());
            when(tokenService.validateToken("t-" + role.name())).thenReturn(user.getEmail() + role.name());
            when(userRepository.findByEmail(user.getEmail() + role.name())).thenReturn(Optional.of(user));

            securityFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

            assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_" + role.name());
        }
    }
}
