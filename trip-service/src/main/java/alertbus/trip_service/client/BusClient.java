package alertbus.trip_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "bus-service", url = "http://localhost:8082")
public interface BusClient {

    @GetMapping("/buses/{id}")
    Object getBusById(@PathVariable("id") Long id);
}
