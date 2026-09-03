package alertbus.route_service.service;

import alertbus.route_service.dto.RouteRequestDTO;
import alertbus.route_service.dto.RouteResponseDTO;
import alertbus.route_service.entity.Route;
import alertbus.route_service.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Service
public class RouteService {

    @Autowired
    private RouteRepository routeRepository;

    @Transactional
    public List<RouteResponseDTO> findAll(){
        return routeRepository.findAll().stream()
                .map(RouteResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public RouteResponseDTO findById(Long id){
        Route route= routeRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Rota não encontrada"));
        return RouteResponseDTO.fromEntity(route);

    }

    @Transactional
    public RouteResponseDTO create(RouteRequestDTO dto){
        if(routeRepository.existsByCodigo(dto.codigo())){
            throw new IllegalArgumentException("Já existe uma rota cadastrada com o código: "+dto.codigo());
        }

        Route route= Route.builder()
                .codigo(dto.codigo())
                .nome(dto.nome())
                .origem(dto.origem())
                .destino(dto.destino())
                .build();

        Route savedRoute= routeRepository.save(route);
        return RouteResponseDTO.fromEntity(savedRoute);
    }
    @Transactional
    public void delete(Long id) {
        if (!routeRepository.existsById(id)) {
            throw new RuntimeException("Rota não encontrada com o ID: " + id);
        }
        routeRepository.deleteById(id);
    }


}
