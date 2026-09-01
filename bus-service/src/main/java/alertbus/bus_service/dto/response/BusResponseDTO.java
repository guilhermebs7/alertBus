package alertbus.bus_service.dto.response;

import alertbus.bus_service.entity.BusStatus;

public record BusResponseDTO(
        Long id,
        String placa,
        String modelo,
        Integer capacidade,
        BusStatus status
) {
}
