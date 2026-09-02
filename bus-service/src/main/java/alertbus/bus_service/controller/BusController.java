package alertbus.bus_service.controller;

import alertbus.bus_service.dto.request.BusRequesDTO;
import alertbus.bus_service.dto.response.BusResponseDTO;
import alertbus.bus_service.entity.BusStatus;
import alertbus.bus_service.service.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buses")
@RequiredArgsConstructor
public class BusController {

    @Autowired
    private BusService busService;

    @PostMapping
    public ResponseEntity<BusResponseDTO> createBus(@RequestBody @Valid BusRequesDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(busService.createdBus(dto));
    }
    @GetMapping
    public ResponseEntity<List<BusResponseDTO>> getAllBuses(){
        return ResponseEntity.ok(busService.getAllBuses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusResponseDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(busService.getBusById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BusResponseDTO> updateBusStatus(
            @PathVariable Long id,
            @RequestParam BusStatus status
            ){
        return ResponseEntity.ok(busService.updateBusStatus(id,status));
    }



}
