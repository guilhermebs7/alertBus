package alertbus.bus_service.service;

import alertbus.bus_service.dto.request.BusRequesDTO;
import alertbus.bus_service.dto.response.BusResponseDTO;
import alertbus.bus_service.entity.Bus;
import alertbus.bus_service.entity.BusStatus;
import alertbus.bus_service.repository.BusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
class BusServiceTest {

    @Mock
    private BusRepository busRepository;

    @InjectMocks
    private BusService busService;

    private Bus bus(Long id, String placa, BusStatus status) {
        return Bus.builder()
                .id(id)
                .placa(placa)
                .modelo("Marcopolo")
                .capacidade(40)
                .status(status)
                .build();
    }


    @Test
    void createdBus_comPlacaNova_deveSalvarEMapearResposta() {
        var dto = new BusRequesDTO("ABC-1234", "Marcopolo", 40, BusStatus.disponivel);
        when(busRepository.findByPlaca("ABC-1234")).thenReturn(Optional.empty());
        when(busRepository.save(any(Bus.class))).thenAnswer(inv -> {
            Bus b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        BusResponseDTO response = busService.createdBus(dto);

        ArgumentCaptor<Bus> captor = ArgumentCaptor.forClass(Bus.class);
        verify(busRepository).save(captor.capture());
        assertThat(captor.getValue().getPlaca()).isEqualTo("ABC-1234");
        assertThat(captor.getValue().getModelo()).isEqualTo("Marcopolo");
        assertThat(captor.getValue().getCapacidade()).isEqualTo(40);
        assertThat(captor.getValue().getStatus()).isEqualTo(BusStatus.disponivel);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.placa()).isEqualTo("ABC-1234");
        assertThat(response.modelo()).isEqualTo("Marcopolo");
        assertThat(response.capacidade()).isEqualTo(40);
        assertThat(response.status()).isEqualTo(BusStatus.disponivel);
    }

    @Test
    void createdBus_comPlacaDuplicada_deveLancarExcecaoENaoSalvar() {
        var dto = new BusRequesDTO("ABC-1234", "Marcopolo", 40, BusStatus.disponivel);
        when(busRepository.findByPlaca("ABC-1234"))
                .thenReturn(Optional.of(bus(1L, "ABC-1234", BusStatus.disponivel)));

        assertThatThrownBy(() -> busService.createdBus(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe um ônibus com essa placa");

        verify(busRepository, never()).save(any());
    }


    @Test
    void getAllBuses_semRegistros_deveRetornarListaVazia() {
        when(busRepository.findAll()).thenReturn(List.of());

        assertThat(busService.getAllBuses()).isEmpty();
    }

    @Test
    void getAllBuses_comRegistros_deveMapearTodos() {
        when(busRepository.findAll()).thenReturn(List.of(
                bus(1L, "AAA-0001", BusStatus.disponivel),
                bus(2L, "BBB-0002", BusStatus.manutencao)));

        List<BusResponseDTO> result = busService.getAllBuses();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BusResponseDTO::placa).containsExactly("AAA-0001", "BBB-0002");
        assertThat(result).extracting(BusResponseDTO::status)
                .containsExactly(BusStatus.disponivel, BusStatus.manutencao);
    }


    @Test
    void getBusById_existente_deveRetornarDTO() {
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus(1L, "AAA-0001", BusStatus.em_viagem)));

        BusResponseDTO result = busService.getBusById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(BusStatus.em_viagem);
    }

    @Test
    void getBusById_inexistente_deveLancarExcecaoComId() {
        when(busRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> busService.getBusById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ônibus não encontrado")
                .hasMessageContaining("99");
    }


    @ParameterizedTest
    @EnumSource(BusStatus.class)
    void updateBusStatus_deveAlterarParaQualquerStatusDoEnum(BusStatus novoStatus) {
        Bus existente = bus(1L, "AAA-0001", BusStatus.disponivel);
        when(busRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(busRepository.save(any(Bus.class))).thenAnswer(inv -> inv.getArgument(0));

        BusResponseDTO result = busService.updateBusStatus(1L, novoStatus);

        verify(busRepository).save(existente);
        assertThat(existente.getStatus()).isEqualTo(novoStatus);
        assertThat(result.status()).isEqualTo(novoStatus);
    }

    @Test
    void updateBusStatus_comIdInexistente_deveLancarExcecaoENaoSalvar() {
        when(busRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> busService.updateBusStatus(99L, BusStatus.inativo))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ônibus não encontrado");

        verify(busRepository, never()).save(any());
    }
}
