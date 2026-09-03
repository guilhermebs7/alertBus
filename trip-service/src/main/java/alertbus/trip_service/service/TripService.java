package alertbus.trip_service.service;

import alertbus.trip_service.client.BusClient;
import alertbus.trip_service.client.RouteClient;
import alertbus.trip_service.dto.TripRequestDTO;
import alertbus.trip_service.dto.TripResponseDTO;
import alertbus.trip_service.entity.Trip;
import alertbus.trip_service.entity.TripStatus;
import alertbus.trip_service.repository.TripRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TripService {

    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private BusClient busClient;
    @Autowired
    private RouteClient routeClient;


    @Transactional
    public List<TripResponseDTO> findAll(){
        return tripRepository.findAll().stream()
                .map(TripResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public TripResponseDTO findById(Long id){
        Trip trip= tripRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Viagem não encontrada"));
        return TripResponseDTO.fromEntity(trip);
    }

    @Transactional
    public TripResponseDTO create(TripRequestDTO dto){
        try {
            busClient.getBusById(dto.busId());
        }catch (FeignException.NotFound e){
            throw  new IllegalArgumentException("Ônibus não encontrado com o ID: "+ dto.busId());
        }

        try {
            routeClient.getRouteById(dto.routeId());
        }catch (FeignException.NotFound e){
            throw new RuntimeException("Rota não encontrada com o ID: "+ dto.routeId());
        }

        Trip trip= Trip.builder()
                .busId(dto.busId())
                .routeId(dto.routeId())
                .status(TripStatus.AGENDADO)
                .startTime(LocalDateTime.now())
                .build();

        return TripResponseDTO.fromEntity(tripRepository.save(trip));
    }
}
