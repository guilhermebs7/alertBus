package alertbus.notification_service.service;

import alertbus.notification_service.dto.TripEventDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationServiceTest {

    private final NotificationService service = new NotificationService();
    private final ByteArrayOutputStream saida = new ByteArrayOutputStream();
    private PrintStream stdoutOriginal;

    @BeforeEach
    void capturarStdout() {
        stdoutOriginal = System.out;
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restaurarStdout() {
        System.setOut(stdoutOriginal);
    }

    private TripEventDTO evento(String status) {
        TripEventDTO e = new TripEventDTO();
        e.setTripId(7L);
        e.setBusId(10L);
        e.setRouteId(3L);
        e.setStatus(status);
        return e;
    }

    @Test
    void sendTripCreatedNotification_deveRegistrarIdDaViagemEDaRota() {
        service.sendTripCreatedNotification(evento("AGENDADO"));

        String log = saida.toString(StandardCharsets.UTF_8);
        assertThat(log).contains("Nova viagem cadastrada");
        assertThat(log).contains("Viagem ID: 7");
        assertThat(log).contains("Rota: 3");
    }

    @Test
    void sendTripStatusNotification_deveRegistrarIdDaViagemENovoStatus() {
        service.sendTripStatusNotification(evento("EM_PROGRESSO"));

        String log = saida.toString(StandardCharsets.UTF_8);
        assertThat(log).contains("Mudança de status na viagem 7");
        assertThat(log).contains("EM_PROGRESSO");
    }
}
