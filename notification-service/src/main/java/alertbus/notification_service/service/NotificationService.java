package alertbus.notification_service.service;

import alertbus.notification_service.dto.TripEventDTO;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void sendTripCreatedNotification(TripEventDTO event){
        System.out.println("[NOTIFICAÇÃO DISPARADA] Nova viagem cadastrada!");
        System.out.println(" Viagem ID: "+ event.getTripId() + " | Linha/Rota: "+ event.getRouteId());
    }

    public void sendTripStatusNotification(TripEventDTO event){
        System.out.println("[ALERTA DE STATUS] Mudança de status na viagem "+ event.getTripId());
        System.out.println(" Novo Sttaus: "+ event.getStatus());
    }
}
