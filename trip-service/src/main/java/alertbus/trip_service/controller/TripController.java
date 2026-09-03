package alertbus.trip_service.controller;

import alertbus.trip_service.dto.TripRequestDTO;
import alertbus.trip_service.dto.TripResponseDTO;
import alertbus.trip_service.service.TripService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private TripService tripService;


    @GetMapping
    public ResponseEntity<List<TripResponseDTO>> findAll(){
        return ResponseEntity.ok(tripService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponseDTO> findById(@PathVariable Long id){
        return ResponseEntity.ok(tripService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TripResponseDTO> create(@RequestBody @Valid TripRequestDTO dto){
        TripResponseDTO created= tripService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
