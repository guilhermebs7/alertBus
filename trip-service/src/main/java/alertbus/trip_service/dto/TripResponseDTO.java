package alertbus.trip_service.dto;

import alertbus.trip_service.entity.Trip;
import alertbus.trip_service.entity.TripStatus;

import java.time.LocalDateTime;

public record TripResponseDTO(
        Long id,
        Long busId,
        Long routeId,
        TripStatus status,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
    public static TripResponseDTO fromEntity(Trip trip) {
        return new TripResponseDTO(
                trip.getId(),
                trip.getBusId(),
                trip.getRouteId(),
                trip.getStatus(),
                trip.getStartTime(),
                trip.getEndTime()
        );
    }
}
