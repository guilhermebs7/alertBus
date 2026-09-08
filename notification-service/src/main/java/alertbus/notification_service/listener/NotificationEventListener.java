package alertbus.notification_service.listener;

import alertbus.notification_service.config.RabbitConfig;
import alertbus.notification_service.dto.TripEventDTO;
import alertbus.notification_service.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    @Autowired
    private NotificationService notificationService;

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_TRIP_QUEUE)
    public void handleTripEvent(TripEventDTO event){
        if("AGENDADO".equalsIgnoreCase(event.getStatus()) || "CRIADO".equalsIgnoreCase(event.getStatus())){
            notificationService.sendTripCreatedNotification(event);
        }else{
            notificationService.sendTripStatusNotification(event);
        }
    }
}
