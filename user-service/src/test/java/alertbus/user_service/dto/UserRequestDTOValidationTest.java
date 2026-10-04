package alertbus.user_service.dto;

import alertbus.user_service.dto.request.UserRequestDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;


class UserRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void dtoValido_naoDeveTerViolacoes() {
        assertThat(validator.validate(new UserRequestDTO("Ana", "ana@alertbus.com", "123456", null))).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void nomeVazio_deveGerarViolacao(String nome) {
        assertThat(validator.validate(new UserRequestDTO(nome, "ana@alertbus.com", "123456", null))).isNotEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void emailVazio_deveGerarViolacao(String email) {
        assertThat(validator.validate(new UserRequestDTO("Ana", email, "123456", null))).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "ana@", "@alertbus.com", "ana alertbus.com"})
    void emailMalFormatado_deveGerarViolacao(String email) {
        assertThat(validator.validate(new UserRequestDTO("Ana", email, "123456", null))).isNotEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void senhaVazia_deveGerarViolacao(String senha) {
        assertThat(validator.validate(new UserRequestDTO("Ana", "ana@alertbus.com", senha, null))).isNotEmpty();
    }
}
