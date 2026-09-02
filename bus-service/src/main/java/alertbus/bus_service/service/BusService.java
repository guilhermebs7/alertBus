package alertbus.bus_service.service;

import alertbus.bus_service.dto.request.BusRequesDTO;
import alertbus.bus_service.dto.response.BusResponseDTO;
import alertbus.bus_service.entity.Bus;
import alertbus.bus_service.entity.BusStatus;
import alertbus.bus_service.repository.BusRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusService {

    @Autowired
    private BusRepository busRepository;

    @Transactional
    public BusResponseDTO createdBus(BusRequesDTO dto){
        if(busRepository.findByPlaca(dto.placa()).isPresent()){
            throw new IllegalArgumentException("Já existe um ônibus com essa placa cadastrada");
        }
        Bus bus= Bus.builder()
                .placa(dto.placa())
                .modelo(dto.modelo())
                .capacidade(dto.capacidade())
                .status(dto.status())
                .build();

        Bus saved = busRepository.save(bus);
        return toResponseDTO(saved);
    }

    public List<BusResponseDTO> getAllBuses(){
        return busRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }
    public BusResponseDTO getBusById(Long id){
        Bus bus= busRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Ônibus não encontrado com o ID: "+ id));
                return toResponseDTO(bus);

    }
    @Transactional
    public BusResponseDTO updateBusStatus(Long id, BusStatus newStatus){
        Bus bus= busRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ônibus não encontrado com o ID:" + id));

        bus.setStatus(newStatus);
        Bus updated= busRepository.save(bus);
        return toResponseDTO(updated);


    }

    private BusResponseDTO toResponseDTO(Bus bus) {
        return new BusResponseDTO(
                bus.getId(),
                bus.getPlaca(),
                bus.getModelo(),
                bus.getCapacidade(),
                bus.getStatus()
        );
    }
}
