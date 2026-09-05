package alert.bus.eta_service.controller;

import alert.bus.eta_service.dto.EtaDTO;
import alert.bus.eta_service.service.EtaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eta")
public class EtaController {

    @Autowired
    private EtaService etaService;

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<EtaDTO> getEtaByTripId(@PathVariable Long tripId){
        EtaDTO eta= etaService.getEtaByTripId(tripId);
        if(eta != null){
            return ResponseEntity.ok(eta);
        }
        return ResponseEntity.notFound().build();
    }
}
