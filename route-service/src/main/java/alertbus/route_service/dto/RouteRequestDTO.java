package alertbus.route_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RouteRequestDTO(
        @NotBlank(message = "O código é obrigatório")
        @Size(max = 20, message = "O código deve ter no máximo 20 caracteres")
        String codigo,

        @NotBlank(message = "O nome é obrigatório")
        String nome,

        String origem,
        String destino
) {
}
