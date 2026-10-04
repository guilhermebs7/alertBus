package alert.bus.eta_service.service;

import alert.bus.eta_service.dto.EtaDTO;
import alert.bus.eta_service.dto.TripEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EtaServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOps;

    private EtaService etaService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        etaService = new EtaService(redisTemplate);
    }

    private TripEventDTO evento(Long tripId, String status) {
        return new TripEventDTO(tripId, 10L, 20L, status, LocalDateTime.now());
    }

    @Test
    void processTripEvent_deveGravarNoRedisComChaveDaViagemETTLDe2Horas() {
        etaService.processTripEvent(evento(5L, "AGENDADO"));

        ArgumentCaptor<EtaDTO> captor = ArgumentCaptor.forClass(EtaDTO.class);
        verify(valueOps).set(eq("ETA:TRIP5"), captor.capture(), eq(Duration.ofHours(2)));

        EtaDTO salvo = captor.getValue();
        assertThat(salvo.tripId()).isEqualTo(5L);
        assertThat(salvo.busId()).isEqualTo(10L);
        assertThat(salvo.routeId()).isEqualTo(20L);
        assertThat(salvo.status()).isEqualTo("AGENDADO");
    }

    @ParameterizedTest
    @CsvSource({
            "AGENDADO,25",
            "EM_PROGRESSO,25",
            "CANCELADA,25",
            "COMPLETED,0"
    })
    void processTripEvent_deveCalcularMinutosRestantesPeloStatus(String status, int minutosEsperados) {
        etaService.processTripEvent(evento(1L, status));

        ArgumentCaptor<EtaDTO> captor = ArgumentCaptor.forClass(EtaDTO.class);
        verify(valueOps).set(eq("ETA:TRIP1"), captor.capture(), any(Duration.class));
        assertThat(captor.getValue().estimatedMinutesRemaining()).isEqualTo(minutosEsperados);
    }


    @Test
    void getEtaByTripId_comValorNoRedis_deveRetornarEtaDTO() {
        EtaDTO eta = new EtaDTO(5L, 10L, 20L, 25, "AGENDADO");
        when(valueOps.get("ETA:TRIP5")).thenReturn(eta);

        assertThat(etaService.getEtaByTripId(5L)).isSameAs(eta);
    }

    @Test
    void getEtaByTripId_semValorNoRedis_deveRetornarNull() {
        when(valueOps.get("ETA:TRIP5")).thenReturn(null);

        assertThat(etaService.getEtaByTripId(5L)).isNull();
    }

    @Test
    void getEtaByTripId_comValorQueNaoEEtaDTO_deveRetornarNull() {
        Map<String, Object> comoMap = new LinkedHashMap<>();
        comoMap.put("tripId", 5);
        when(valueOps.get("ETA:TRIP5")).thenReturn(comoMap);

        assertThat(etaService.getEtaByTripId(5L)).isNull();
    }
}
