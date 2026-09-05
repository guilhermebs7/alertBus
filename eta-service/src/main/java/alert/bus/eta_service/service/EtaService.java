package alert.bus.eta_service.service;

import alert.bus.eta_service.dto.EtaDTO;
import alert.bus.eta_service.dto.TripEventDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class EtaService {

    private final RedisTemplate<String,Object> redisTemplate;
    private static final String ETA_KEY_PREFIX = "ETA:TRIP";

    public EtaService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void processTripEvent(TripEventDTO event){

        //exemplo inicial de cálculo: define um ETA padrão baseado no evento
        int defaultMinutes= "COMPLETED".equals(event.status())? 0 : 25;

        EtaDTO eta = new EtaDTO(
                event.tripId(),
                event.busId(),
                event.routeId(),
                defaultMinutes,
                event.status()
        );

        redisTemplate.opsForValue().set(ETA_KEY_PREFIX + event.tripId(), eta, Duration.ofHours(2));   // salva no Reedis com expiração de 2 horas
        System.out.println("ETA atualizado no Redis para a viagem: " + event.tripId());
    }
    public EtaDTO getEtaByTripId(Long tripId){
        Object obj = redisTemplate.opsForValue().get(ETA_KEY_PREFIX + tripId);
        if(obj instanceof  EtaDTO eta){
            return eta;
        }
        return null;
    }
    }

