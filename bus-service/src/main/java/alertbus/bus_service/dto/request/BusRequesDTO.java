package alertbus.bus_service.dto.request;

import alertbus.bus_service.entity.BusStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BusRequesDTO(
        @NotBlank(message = "A placa é obrigatória") String placa,
        String modelo,
        Integer capacidade,
        @NotNull(message = "O status é obrigatório")BusStatus status
        ) {
}
