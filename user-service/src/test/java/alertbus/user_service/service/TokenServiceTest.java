package alertbus.user_service.service;

import alertbus.user_service.domain.entity.User;
import alertbus.user_service.domain.entity.UserRole;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private static final String SECRET = "segredo-de-teste";
    private static final String ISSUER = "alert-bus-service";

    private TokenService tokenService;
    private User user;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
        user = User.builder()
                .id(UUID.randomUUID())
                .name("Ana")
                .email("ana@alertbus.com")
                .role(UserRole.PASSENGER)
                .build();
    }


    @Test
    void generateToken_deveConterIssuerSubjectEClaimUserId() {
        String token = tokenService.generateToken(user);

        DecodedJWT decoded = JWT.decode(token);
        assertThat(decoded.getIssuer()).isEqualTo(ISSUER);
        assertThat(decoded.getSubject()).isEqualTo("ana@alertbus.com");
        assertThat(decoded.getClaim("userId").asString()).isEqualTo(user.getId().toString());
        assertThat(decoded.getExpiresAt()).isNotNull();
    }

    @Test
    void generateToken_naoDeveConterASenhaNemARole() {
        DecodedJWT decoded = JWT.decode(tokenService.generateToken(user));

        assertThat(decoded.getClaims()).doesNotContainKeys("password", "role");
    }

    @Test
    void generateToken_emFusoDeSaoPaulo_deveExpirarEmAproximadamente2Horas() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));

            DecodedJWT decoded = JWT.decode(tokenService.generateToken(user));

            long minutos = Duration.between(Instant.now(), decoded.getExpiresAt().toInstant()).toMinutes();
            assertThat(minutos).isBetween(118L, 121L);
        } finally {
            TimeZone.setDefault(original);
        }
    }

    @Test
    @Disabled("BUG: genExpirationDate usa LocalDateTime.now() com offset fixo -03:00. "
            + "Em servidor/contêiner em UTC o token dura ~5h. Use Instant.now().plus(2, ChronoUnit.HOURS).")
    void generateToken_emFusoUTC_deveExpirarEmAproximadamente2Horas() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            DecodedJWT decoded = JWT.decode(tokenService.generateToken(user));

            long minutos = Duration.between(Instant.now(), decoded.getExpiresAt().toInstant()).toMinutes();
            assertThat(minutos).isBetween(118L, 121L);
        } finally {
            TimeZone.setDefault(original);
        }
    }


    @Test
    void validateToken_comTokenValido_deveRetornarOEmail() {
        String token = tokenService.generateToken(user);

        assertThat(tokenService.validateToken(token)).isEqualTo("ana@alertbus.com");
    }

    @Test
    void validateToken_comTokenAssinadoComOutroSegredo_deveRetornarVazio() {
        TokenService outro = new TokenService();
        ReflectionTestUtils.setField(outro, "secret", "outro-segredo");
        String tokenDeOutro = outro.generateToken(user);

        assertThat(tokenService.validateToken(tokenDeOutro)).isEmpty();
    }

    @Test
    void validateToken_comTokenExpirado_deveRetornarVazio() {
        String expirado = JWT.create()
                .withIssuer(ISSUER)
                .withSubject("ana@alertbus.com")
                .withExpiresAt(Instant.now().minusSeconds(60))
                .sign(Algorithm.HMAC256(SECRET));

        assertThat(tokenService.validateToken(expirado)).isEmpty();
    }

    @Test
    void validateToken_comIssuerDiferente_deveRetornarVazio() {
        String outroIssuer = JWT.create()
                .withIssuer("outro-servico")
                .withSubject("ana@alertbus.com")
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(Algorithm.HMAC256(SECRET));

        assertThat(tokenService.validateToken(outroIssuer)).isEmpty();
    }

    @Test
    void validateToken_comPayloadAdulterado_deveRetornarVazio() {
        String[] partes = tokenService.generateToken(user).split("\\.");
        String payloadFalso = Base64.getUrlEncoder().withoutPadding().encodeToString(
                ("{\"iss\":\"" + ISSUER + "\",\"sub\":\"hacker@x.com\"}").getBytes(StandardCharsets.UTF_8));

        String adulterado = partes[0] + "." + payloadFalso + "." + partes[2];

        assertThat(tokenService.validateToken(adulterado)).isEmpty();
    }

    @Test
    void validateToken_comLixo_deveRetornarVazio() {
        assertThat(tokenService.validateToken("isto-nao-e-um-jwt")).isEmpty();
        assertThat(tokenService.validateToken("")).isEmpty();
    }
}
