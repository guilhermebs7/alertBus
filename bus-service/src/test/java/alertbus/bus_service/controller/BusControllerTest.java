package alertbus.bus_service.controller;

import alertbus.bus_service.dto.request.BusRequesDTO;
import alertbus.bus_service.dto.response.BusResponseDTO;
import alertbus.bus_service.entity.BusStatus;
import alertbus.bus_service.service.BusService;
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
class BusControllerTest {

    @Mock
    private BusService busService;

    @InjectMocks
    private BusController busController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(busController).build();
    }

    @Test
    void post_valido_deveRetornar201ECorpo() throws Exception {
        when(busService.createdBus(any(BusRequesDTO.class)))
                .thenReturn(new BusResponseDTO(1L, "ABC-1234", "Marcopolo", 40, BusStatus.disponivel));

        mockMvc.perform(post("/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC-1234\",\"modelo\":\"Marcopolo\",\"capacidade\":40,\"status\":\"disponivel\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.placa").value("ABC-1234"))
                .andExpect(jsonPath("$.status").value("disponivel"));
    }

    @Test
    void post_comPlacaEmBranco_deveRetornar400ENaoChamarService() throws Exception {
        mockMvc.perform(post("/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"\",\"status\":\"disponivel\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(busService);
    }

    @Test
    void post_semStatus_deveRetornar400() throws Exception {
        mockMvc.perform(post("/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"ABC-1234\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(busService);
    }

    @Test
    void get_deveRetornar200ComLista() throws Exception {
        when(busService.getAllBuses()).thenReturn(List.of(
                new BusResponseDTO(1L, "AAA-0001", "M1", 30, BusStatus.disponivel),
                new BusResponseDTO(2L, "BBB-0002", "M2", 40, BusStatus.inativo)));

        mockMvc.perform(get("/buses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].placa").value("AAA-0001"));
    }

    @Test
    void getPorId_deveRetornar200() throws Exception {
        when(busService.getBusById(1L))
                .thenReturn(new BusResponseDTO(1L, "AAA-0001", "M1", 30, BusStatus.em_viagem));

        mockMvc.perform(get("/buses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("em_viagem"));
    }

    @Test
    void patchStatus_valido_deveChamarServiceComEnumCorreto() throws Exception {
        when(busService.updateBusStatus(1L, BusStatus.manutencao))
                .thenReturn(new BusResponseDTO(1L, "AAA-0001", "M1", 30, BusStatus.manutencao));

        mockMvc.perform(patch("/buses/1/status").param("status", "manutencao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("manutencao"));

        verify(busService).updateBusStatus(1L, BusStatus.manutencao);
    }

    @Test
    void patchStatus_comValorInvalido_deveRetornar400() throws Exception {
        mockMvc.perform(patch("/buses/1/status").param("status", "xyz"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(busService);
    }
}
