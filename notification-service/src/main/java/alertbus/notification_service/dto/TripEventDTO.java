package alertbus.notification_service.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TripEventDTO implements Serializable {
    private Long tripId;
    private Long busId;
    private Long routeId;
    private String status;
    private LocalDateTime timestamp;

    public TripEventDTO() {}

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Long getBusId() {
        return busId;
    }

    public void setBusId(Long busId) {
        this.busId = busId;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TripEventDTO{" +
                "tripId=" + tripId +
                ", busId=" + busId +
                ", routeId=" + routeId +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
