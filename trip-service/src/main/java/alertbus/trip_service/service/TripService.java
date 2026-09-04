package alertbus.trip_service.service;

import alertbus.trip_service.client.BusClient;
import alertbus.trip_service.client.RouteClient;
import alertbus.trip_service.config.RabbitMQConfig;
import alertbus.trip_service.dto.TripEventDTO;
import alertbus.trip_service.dto.TripRequestDTO;
import alertbus.trip_service.dto.TripResponseDTO;
import alertbus.trip_service.entity.Trip;
import alertbus.trip_service.entity.TripStatus;
import alertbus.trip_service.repository.TripRepository;
import feign.FeignException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    @Autowired
    private RabbitTemplate rabbitTemplate;

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

        Trip savedTrip= tripRepository.save(trip);

        TripEventDTO event= new TripEventDTO(
                savedTrip.getId(),
                savedTrip.getBusId(),
                savedTrip.getRouteId(),
                savedTrip.getStatus(),
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.TRIP_EXCHANGE,RabbitMQConfig.ROUTING_KEY_CREATED,event);

        return TripResponseDTO.fromEntity(savedTrip);
    }
    @Transactional
    public TripResponseDTO updateStatus(Long id, TripStatus status) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada"));

        trip.setStatus(status);

        if (status == TripStatus.COMPLETADA|| status == TripStatus.CANCELADA) {
            trip.setEndTime(LocalDateTime.now());
        }

        Trip savedTrip = tripRepository.save(trip);

        TripEventDTO event = new TripEventDTO(
                savedTrip.getId(),
                savedTrip.getBusId(),
                savedTrip.getRouteId(),
                savedTrip.getStatus(),
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.TRIP_EXCHANGE, RabbitMQConfig.ROUTING_KEY_STATUS, event);

        return TripResponseDTO.fromEntity(savedTrip);
    }

}
