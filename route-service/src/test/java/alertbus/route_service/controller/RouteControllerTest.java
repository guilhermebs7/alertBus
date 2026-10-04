package alertbus.route_service.controller;

import alertbus.route_service.dto.RouteRequestDTO;
import alertbus.route_service.dto.RouteResponseDTO;
import alertbus.route_service.service.RouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RouteControllerTest {

    @Mock
    private RouteService routeService;

    @InjectMocks
    private RouteController routeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(routeController).build();
    }

    @Test
    void get_deveRetornar200ComLista() throws Exception {
        when(routeService.findAll()).thenReturn(List.of(
                new RouteResponseDTO(1L, "A1", "Rota A1", "Centro", "Boa Viagem"),
                new RouteResponseDTO(2L, "B2", "Rota B2", "Olinda", "Recife")));

        mockMvc.perform(get("/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].codigo").value("B2"));
    }

    @Test
    void getPorId_deveRetornar200() throws Exception {
        when(routeService.findById(1L))
                .thenReturn(new RouteResponseDTO(1L, "A1", "Rota A1", "Centro", "Boa Viagem"));

        mockMvc.perform(get("/routes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Rota A1"));
    }

    @Test
    void post_valido_deveRetornar201() throws Exception {
        when(routeService.create(any(RouteRequestDTO.class)))
                .thenReturn(new RouteResponseDTO(1L, "A1", "Rota A1", "Centro", "Boa Viagem"));

        mockMvc.perform(post("/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"A1\",\"nome\":\"Rota A1\",\"origem\":\"Centro\",\"destino\":\"Boa Viagem\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("A1"));
    }

    @Test
    void post_semCodigo_deveRetornar400() throws Exception {
        mockMvc.perform(post("/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Rota sem código\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(routeService);
    }

    @Test
    void post_comCodigoMaiorQue20_deveRetornar400() throws Exception {
        String codigoLongo = "X".repeat(21);

        mockMvc.perform(post("/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"" + codigoLongo + "\",\"nome\":\"Rota\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(routeService);
    }

    @Test
    void delete_deveRetornar204ChamandoService() throws Exception {
        mockMvc.perform(delete("/routes/1"))
                .andExpect(status().isNoContent());

        verify(routeService).delete(1L);
    }
}
