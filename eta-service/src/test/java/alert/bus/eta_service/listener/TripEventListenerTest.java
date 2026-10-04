package alert.bus.eta_service.listener;

import alert.bus.eta_service.dto.TripEventDTO;
import alert.bus.eta_service.service.EtaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripEventListenerTest {

    @Mock
    private EtaService etaService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TripEventListener listener;

    private TripEventDTO evento(Long tripId, String status) {
        return new TripEventDTO(tripId, 10L, 20L, status, LocalDateTime.now());
    }

    @Test
    void handleTripCreated_deveProcessarNoEtaServiceENotificarNoWebSocket() {
        TripEventDTO event = evento(7L, "AGENDADO");

        listener.handleTripCreated(event);

        InOrder ordem = inOrder(etaService, messagingTemplate);
        ordem.verify(etaService).processTripEvent(event);
        ordem.verify(messagingTemplate).convertAndSend("/topic/eta/7", event);
    }

    @Test
    void handleTripStatusUpdated_deveProcessarNoEtaServiceENotificarNoWebSocket() {
        TripEventDTO event = evento(8L, "EM_PROGRESSO");

        listener.handleTripStatusUpdated(event);

        InOrder ordem = inOrder(etaService, messagingTemplate);
        ordem.verify(etaService).processTripEvent(event);
        ordem.verify(messagingTemplate).convertAndSend("/topic/eta/8", event);
    }

    @Test
    void cadaViagemDeveUsarOSeuProprioTopico() {
        TripEventDTO a = evento(1L, "AGENDADO");
        TripEventDTO b = evento(2L, "AGENDADO");

        listener.handleTripCreated(a);
        listener.handleTripCreated(b);

        verify(messagingTemplate).convertAndSend("/topic/eta/1", a);
        verify(messagingTemplate).convertAndSend("/topic/eta/2", b);
        verifyNoMoreInteractions(messagingTemplate);
    }
}
