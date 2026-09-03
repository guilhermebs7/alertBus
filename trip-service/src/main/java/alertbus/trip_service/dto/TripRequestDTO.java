package alertbus.trip_service.dto;

import jakarta.validation.constraints.NotNull;

public record TripRequestDTO(
        @NotNull(message = "O ID do ônibus é obrigatório")
        Long busId,

        @NotNull(message = "O ID da rota é obrigatório")
        Long routeId
) {
}
