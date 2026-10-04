package alertbus.notification_service.listener;

import alertbus.notification_service.dto.TripEventDTO;
import alertbus.notification_service.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener listener;

    private TripEventDTO evento(String status) {
        TripEventDTO e = new TripEventDTO();
        e.setTripId(1L);
        e.setBusId(10L);
        e.setRouteId(20L);
        e.setStatus(status);
        e.setTimestamp(LocalDateTime.now());
        return e;
    }

    @ParameterizedTest
    @ValueSource(strings = {"AGENDADO", "agendado", "Agendado", "CRIADO", "criado"})
    void statusDeCriacao_deveDisparar_notificacaoDeViagemCriada(String status) {
        TripEventDTO event = evento(status);

        listener.handleTripEvent(event);

        verify(notificationService).sendTripCreatedNotification(event);
        verify(notificationService, never()).sendTripStatusNotification(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"EM_PROGRESSO", "COMPLETADA", "CANCELADA", "QUALQUER_OUTRO"})
    void demaisStatus_deveDisparar_notificacaoDeMudancaDeStatus(String status) {
        TripEventDTO event = evento(status);

        listener.handleTripEvent(event);

        verify(notificationService).sendTripStatusNotification(event);
        verify(notificationService, never()).sendTripCreatedNotification(any());
    }

    @ParameterizedTest
    @NullSource
    void statusNulo_deveCairNaNotificacaoDeStatusSemLancarExcecao(String status) {
        TripEventDTO event = evento(status);

        listener.handleTripEvent(event);

        verify(notificationService).sendTripStatusNotification(event);
    }
}
