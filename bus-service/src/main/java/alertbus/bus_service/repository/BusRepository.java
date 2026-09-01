package alertbus.bus_service.repository;

import alertbus.bus_service.entity.Bus;
import alertbus.bus_service.entity.BusStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus,Long> {
    Optional<Bus> findByPlaca(String placa);
    List<Bus> findByStatus(BusStatus status);
}
