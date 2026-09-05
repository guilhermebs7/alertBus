package alert.bus.eta_service.dto;

import java.time.LocalDateTime;

public record TripEventDTO(
        Long tripId,
        Long busId,
        Long routeId,
        String status,
        LocalDateTime timestamp
) {
}
