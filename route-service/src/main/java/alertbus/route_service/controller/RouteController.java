package alertbus.route_service.controller;

import alertbus.route_service.dto.RouteRequestDTO;
import alertbus.route_service.dto.RouteResponseDTO;
import alertbus.route_service.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
public class RouteController {
    @Autowired
    private RouteService routeService;


    @GetMapping
    public ResponseEntity<List<RouteResponseDTO>> getAll(){
        return ResponseEntity.ok(routeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponseDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(routeService.findById(id));
    }
    @PostMapping
    public ResponseEntity<RouteResponseDTO> create(@RequestBody @Valid RouteRequestDTO dto) {
        RouteResponseDTO response = routeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
