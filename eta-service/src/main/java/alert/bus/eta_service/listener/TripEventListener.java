package alert.bus.eta_service.listener;

import alert.bus.eta_service.dto.TripEventDTO;
import alert.bus.eta_service.service.EtaService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class TripEventListener {

    private final EtaService etaService;
    private final SimpMessagingTemplate messagingTemplate;

    // Injeção limpa de dependências via Construtor (Boa Prática)
    public TripEventListener(EtaService etaService, SimpMessagingTemplate messagingTemplate) {
        this.etaService = etaService;
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = "trip.created.queue")
    public void handleTripCreated(TripEventDTO event) {
        System.out.println("Recebido evento TRIP_CREATED: " + event);

        // 1. O próprio serviço cuida de salvar/processar no Redis
        etaService.processTripEvent(event);

        // 2. Dispara no WebSocket para o React atualizar a tela sozinho
        messagingTemplate.convertAndSend("/topic/eta/" + event.tripId(), event);
    }

    @RabbitListener(queues = "trip.status.queue")
    public void handleTripStatusUpdated(TripEventDTO event) {
        System.out.println("Recebido evento TRIP STATUS UPDATED: " + event);

        // 1. Processa a mudança de status no Redis
        etaService.processTripEvent(event);

        // 2. Notifica o WebSocket também sobre a mudança de status
        messagingTemplate.convertAndSend("/topic/eta/" + event.tripId(), event);
    }
}