package alert.bus.eta_service.dto;

public record EtaDTO(
        Long tripId,
        Long busId,
        Long routeId,
        int estimatedMinutesRemaining,
        String status
) {
}
