package alertbus.trip_service.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;       //validator é o objeto que efetivamente vai verificar se o DTO possui erros de validação
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TripRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();   //cria um mecanismo padrão de validação
    }

    @Test
    void dtoValido_naoDeveTerViolacoes() {
        assertThat(validator.validate(new TripRequestDTO(1L, 2L))).isEmpty();
    }

    @Test
    void busIdNulo_deveGerarViolacao() {
        assertThat(validator.validate(new TripRequestDTO(null, 2L)))
                .extracting(v -> v.getMessage())
                .containsExactly("O ID do ônibus é obrigatório");
    }

    @Test
    void routeIdNulo_deveGerarViolacao() {
        assertThat(validator.validate(new TripRequestDTO(1L, null)))
                .extracting(v -> v.getMessage())
                .containsExactly("O ID da rota é obrigatório");
    }

    @Test
    void ambosNulos_deveGerarDuasViolacoes() {
        assertThat(validator.validate(new TripRequestDTO(null, null))).hasSize(2);
    }
}