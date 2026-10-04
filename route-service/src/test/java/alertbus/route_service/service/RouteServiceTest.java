package alertbus.route_service.service;

import alertbus.route_service.dto.RouteRequestDTO;
import alertbus.route_service.dto.RouteResponseDTO;
import alertbus.route_service.entity.Route;
import alertbus.route_service.repository.RouteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private RouteService routeService;

    private Route route(Long id, String codigo) {
        return Route.builder()
                .id(id)
                .codigo(codigo)
                .nome("Rota " + codigo)
                .origem("Centro")
                .destino("Boa Viagem")
                .build();
    }


    @Test
    void findAll_semRegistros_deveRetornarListaVazia() {
        when(routeRepository.findAll()).thenReturn(List.of());

        assertThat(routeService.findAll()).isEmpty();
    }

    @Test
    void findAll_comRegistros_deveMapearTodos() {
        when(routeRepository.findAll()).thenReturn(List.of(route(1L, "A1"), route(2L, "B2")));

        List<RouteResponseDTO> result = routeService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RouteResponseDTO::codigo).containsExactly("A1", "B2");
    }


    @Test
    void findById_existente_deveRetornarDTO() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route(1L, "A1")));

        RouteResponseDTO result = routeService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.codigo()).isEqualTo("A1");
        assertThat(result.origem()).isEqualTo("Centro");
        assertThat(result.destino()).isEqualTo("Boa Viagem");
    }

    @Test
    void findById_inexistente_deveLancarExcecao() {
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routeService.findById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rota não encontrada");
    }


    @Test
    void create_comCodigoNovo_deveSalvarERetornarDTO() {
        var dto = new RouteRequestDTO("A1", "Rota A1", "Centro", "Boa Viagem");
        when(routeRepository.existsByCodigo("A1")).thenReturn(false);
        when(routeRepository.save(any(Route.class))).thenAnswer(inv -> {
            Route r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });

        RouteResponseDTO result = routeService.create(dto);

        ArgumentCaptor<Route> captor = ArgumentCaptor.forClass(Route.class);
        verify(routeRepository).save(captor.capture());
        assertThat(captor.getValue().getCodigo()).isEqualTo("A1");
        assertThat(captor.getValue().getNome()).isEqualTo("Rota A1");
        assertThat(captor.getValue().getOrigem()).isEqualTo("Centro");
        assertThat(captor.getValue().getDestino()).isEqualTo("Boa Viagem");

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.codigo()).isEqualTo("A1");
    }

    @Test
    void create_comCodigoDuplicado_deveLancarExcecaoENaoSalvar() {
        var dto = new RouteRequestDTO("A1", "Rota A1", "Centro", "Boa Viagem");
        when(routeRepository.existsByCodigo("A1")).thenReturn(true);

        assertThatThrownBy(() -> routeService.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe uma rota cadastrada com o código")
                .hasMessageContaining("A1");

        verify(routeRepository, never()).save(any());
    }


    @Test
    void delete_existente_deveChamarDeleteById() {
        when(routeRepository.existsById(1L)).thenReturn(true);

        routeService.delete(1L);

        verify(routeRepository).deleteById(1L);
    }

    @Test
    void delete_inexistente_deveLancarExcecaoENaoChamarDeleteById() {
        when(routeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> routeService.delete(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rota não encontrada")
                .hasMessageContaining("99");

        verify(routeRepository, never()).deleteById(anyLong());
    }
}
