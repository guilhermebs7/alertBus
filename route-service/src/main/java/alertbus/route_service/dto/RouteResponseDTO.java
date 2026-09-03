package alertbus.route_service.dto;

import alertbus.route_service.entity.Route;

public record RouteResponseDTO(
        Long id,
        String codigo,
        String nome,
        String origem,
        String destino
) {
    public static RouteResponseDTO fromEntity(Route route) {
        return new RouteResponseDTO(
                route.getId(),
                route.getCodigo(),
                route.getNome(),
                route.getOrigem(),
                route.getDestino()
        );
    }
}
