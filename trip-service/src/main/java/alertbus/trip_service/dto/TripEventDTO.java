package alertbus.trip_service.dto;

import alertbus.trip_service.entity.TripStatus;

import java.time.LocalDateTime;

public record TripEventDTO(
        Long tripId,
        Long busId,
        Long routeId,
        TripStatus status,
        LocalDateTime timestamp
) {
}
