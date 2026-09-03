package alertbus.route_service.repository;

import alertbus.route_service.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route,Long> {
    boolean existsByCodigo(String codigo);
    Optional<Route> findByCodigo(String codigo);
}
