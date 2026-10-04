package alert.bus.eta_service.controller;

import alert.bus.eta_service.dto.EtaDTO;
import alert.bus.eta_service.service.EtaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EtaControllerTest {

    @Mock
    private EtaService etaService;

    @InjectMocks
    private EtaController etaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(etaController).build();
    }

    @Test
    void get_comEtaExistente_deveRetornar200ComCorpo() throws Exception {
        when(etaService.getEtaByTripId(5L)).thenReturn(new EtaDTO(5L, 10L, 20L, 25, "AGENDADO"));

        mockMvc.perform(get("/eta/trip/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripId").value(5))
                .andExpect(jsonPath("$.estimatedMinutesRemaining").value(25))
                .andExpect(jsonPath("$.status").value("AGENDADO"));
    }

    @Test
    void get_semEta_deveRetornar404() throws Exception {
        when(etaService.getEtaByTripId(99L)).thenReturn(null);

        mockMvc.perform(get("/eta/trip/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void get_comIdNaoNumerico_deveRetornar400() throws Exception {
        mockMvc.perform(get("/eta/trip/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(etaService);
    }
}
