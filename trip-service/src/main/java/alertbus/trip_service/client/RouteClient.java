package alertbus.trip_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "route-service", url = "http://localhost:8083")
public interface RouteClient {

    @GetMapping("/routes/{id}")
    Object getRouteById(@PathVariable("id") Long id);
}
