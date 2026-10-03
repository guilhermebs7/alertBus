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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)   //usar o mockito neste teste e inicializa os mocks automaticamente
class TripServiceTest {

    @Mock             //cria um objeto falso ,durante o teste n estamos usando o bd
    private TripRepository tripRepository;

    @Mock
    private BusClient busClient;      //responsável por conversar com o servico do bus

    @Mock
    private RouteClient routeClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks                //o Mockito cria uma instância real de TripService e coloca dentro dela os mocks que criamos
    private TripService tripService;

    private Trip trip(Long id, TripStatus status) {  // método auxiliar serve para facilitar a criação de obejtos Trip durante os testes
        return Trip.builder()
                .id(id)
                .busId(10L)
                .routeId(20L)
                .status(status)
                .startTime(LocalDateTime.now().minusMinutes(10))
                .build();
    }
    private void stubSaveComId(Long id) {          //método configura o comportamento do mock
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            if (t.getId() == null) {
                t.setId(id);
            }
            return t;
        });
    }
    @Test
    void findAll_deveMapearTodasAsViagens(){
        when(tripRepository.findAll()).thenReturn(List.of(trip(1L, TripStatus.AGENDADO), trip(2L, TripStatus.EM_PROGRESSO)));  //quando o trip.repository.findALL for chamado retorne duas chamadas

        List<TripResponseDTO> result = tripService.findAll();     //executa o método real do service. como o repository é mockado  ele revebe a lista que configuramos
        assertThat(result).hasSize(2);                //o resultado possui 2 elementos?
        assertThat(result).extracting(TripResponseDTO::status)           //pega somente o campo status de cada DTO
                .containsExactly(TripStatus.AGENDADO, TripStatus.EM_PROGRESSO);
    }
    @Test
    void findById_existente_deveRetornarDTO() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip(1L, TripStatus.AGENDADO)));

        TripResponseDTO result = tripService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.busId()).isEqualTo(10L);
        assertThat(result.routeId()).isEqualTo(20L);
    }


    @Test
    void findById_inexistente_deveLancarExcecao() {
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());  //procura uma viagem com esse id e ele retorna vazio

        assertThatThrownBy(() -> tripService.findById(99L)) // aqui esperamos que o método lance uma exceção
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Viagem não encontrada");
    }
    @Test
    void create_comBusERotaExistentes_deveSalvarComoAgendadoComStartTime() {
        stubSaveComId(1L);

        TripResponseDTO result = tripService.create(new TripRequestDTO(10L, 20L));

        ArgumentCaptor<Trip> captor = ArgumentCaptor.forClass(Trip.class);   //ArgumentCaptor serve para capturar o objeto que foi passado para um mock
        verify(tripRepository).save(captor.capture());
        Trip salva = captor.getValue();
        assertThat(salva.getBusId()).isEqualTo(10L);
        assertThat(salva.getRouteId()).isEqualTo(20L);
        assertThat(salva.getStatus()).isEqualTo(TripStatus.AGENDADO);
        assertThat(salva.getStartTime()).isNotNull();
        assertThat(salva.getEndTime()).isNull();

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(TripStatus.AGENDADO);
    }

    @Test
    void create_deveValidarBusERotaPeloId() {
        stubSaveComId(1L);

        tripService.create(new TripRequestDTO(10L, 20L));

        verify(busClient).getBusById(10L);
        verify(routeClient).getRouteById(20L);
    }

    @Test
    void create_devePublicarEventoComRoutingKeyCreated() {
        stubSaveComId(1L);

        tripService.create(new TripRequestDTO(10L, 20L));

        ArgumentCaptor<TripEventDTO> captor = ArgumentCaptor.forClass(TripEventDTO.class);   //queremos captar o evento enviado para o RabbitMQ.
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.TRIP_EXCHANGE),         //eq() : o argumento precisa ser exatamente esse valor
                eq(RabbitMQConfig.ROUTING_KEY_CREATED),
                captor.capture());

        TripEventDTO evento = captor.getValue();
        assertThat(evento.tripId()).isEqualTo(1L);
        assertThat(evento.busId()).isEqualTo(10L);
        assertThat(evento.routeId()).isEqualTo(20L);
        assertThat(evento.status()).isEqualTo(TripStatus.AGENDADO);
        assertThat(evento.timestamp()).isNotNull();
    }
    @Test
    void create_comOnibusInexistente_deveLancarIllegalArgumentENaoSalvarNemPublicar() {
        when(busClient.getBusById(10L)).thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> tripService.create(new TripRequestDTO(10L, 20L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ônibus não encontrado")
                .hasMessageContaining("10");

        verifyNoInteractions(tripRepository, rabbitTemplate, routeClient);   //como o bus não existe , nenhuma interação deve ter acontecido com esses mocks.
    }
    @Test
    void create_comRotaInexistente_deveLancarExcecaoENaoSalvarNemPublicar() {
        when(routeClient.getRouteById(20L)).thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> tripService.create(new TripRequestDTO(10L, 20L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rota não encontrada")
                .hasMessageContaining("20");

        verifyNoInteractions(tripRepository, rabbitTemplate);
    }
    @Test
    void create_quandoBusServiceFalhaComErroDeServidor_deveRepassarAExcecaoOriginal() {
        FeignException erro = mock(FeignException.InternalServerError.class);
        when(busClient.getBusById(10L)).thenThrow(erro);

        assertThatThrownBy(() -> tripService.create(new TripRequestDTO(10L, 20L)))
                .isSameAs(erro);  // a exceção lançada deve ser exatamente o mesmo objeto erro.

        verifyNoInteractions(tripRepository, rabbitTemplate);
    }
    @Test
    void updateStatus_paraEmProgresso_naoDevePreencherEndTime() {
        Trip existente = trip(1L, TripStatus.AGENDADO);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        TripResponseDTO result = tripService.updateStatus(1L, TripStatus.EM_PROGRESSO);

        assertThat(result.status()).isEqualTo(TripStatus.EM_PROGRESSO);
        assertThat(result.endTime()).isNull();
    }
    @ParameterizedTest      //execute o mesmo teste variás vezes usando valores diferentes
    @EnumSource(value = TripStatus.class, names = {"COMPLETADA", "CANCELADA"})    //aqui o teste será executado 2 vezes  uma com o COMPLETADA e outro com o CANCELADA
    void updateStatus_paraStatusFinal_devePreencherEndTime(TripStatus statusFinal) {
        Trip existente = trip(1L, TripStatus.EM_PROGRESSO);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        TripResponseDTO result = tripService.updateStatus(1L, statusFinal);

        assertThat(result.status()).isEqualTo(statusFinal);
        assertThat(result.endTime()).isNotNull();
    }
    @Test
    void updateStatus_devePublicarEventoComRoutingKeyStatusUpdated() {
        Trip existente = trip(1L, TripStatus.AGENDADO);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        tripService.updateStatus(1L, TripStatus.EM_PROGRESSO);

        ArgumentCaptor<TripEventDTO> captor = ArgumentCaptor.forClass(TripEventDTO.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.TRIP_EXCHANGE),
                eq(RabbitMQConfig.ROUTING_KEY_STATUS),
                captor.capture());
        assertThat(captor.getValue().tripId()).isEqualTo(1L);
        assertThat(captor.getValue().status()).isEqualTo(TripStatus.EM_PROGRESSO);
    }
    @Test
    void updateStatus_comIdInexistente_deveLancarExcecaoENaoSalvarNemPublicar() {
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.updateStatus(99L, TripStatus.CANCELADA))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Viagem não encontrada");

        verify(tripRepository, never()).save(any());
        verifyNoInteractions(rabbitTemplate);
    }













}




