package alertbus.trip_service.controller;
import alertbus.trip_service.dto.TripRequestDTO;
import alertbus.trip_service.dto.TripResponseDTO;
import alertbus.trip_service.entity.TripStatus;
import alertbus.trip_service.exception.GlobalExceptionHandler;
import alertbus.trip_service.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;


import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TripControllerTest {

    @Mock
    private TripService tripService;

    @InjectMocks
    private TripController tripController;

    private MockMvc mockMvc;    //permite simular requisições HTTP sem subir um servidor real

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.standaloneSetup(tripController)  //crie um MockMvc usando este controller
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }
    private TripResponseDTO dto(Long id , TripStatus status){
        return  new TripResponseDTO(id, 10L, 20L, status, null, null);
    }
    @Test
    void get_deveRetornar200ComLista() throws Exception {
        when(tripService.findAll()).thenReturn(List.of(dto(1L, TripStatus.AGENDADO), dto(2L, TripStatus.CANCELADA)));

        mockMvc.perform(get("/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].status").value("CANCELADA"));
    }
    @Test
    void getPorId_existente_deveRetornar200() throws Exception {
        when(tripService.findById(1L)).thenReturn(dto(1L, TripStatus.EM_PROGRESSO));

        mockMvc.perform(get("/trips/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.busId").value(10))
                .andExpect(jsonPath("$.routeId").value(20));
    }
    @Test
    void getPorId_inexistente_deveRetornar404ComCorpoDeErro() throws Exception {
        when(tripService.findById(99L)).thenThrow(new RuntimeException("Viagem não encontrada"));

        mockMvc.perform(get("/trips/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Viagem não encontrada"));
    }
    @Test
    void post_valido_deveRetornar201() throws Exception {
        when(tripService.create(any(TripRequestDTO.class))).thenReturn(dto(1L, TripStatus.AGENDADO));

        mockMvc.perform(post("/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"busId\":10,\"routeId\":20}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADO"));
    }
    @Test
    void post_semBusId_deveRetornar400ENaoChamarService() throws Exception {
        mockMvc.perform(post("/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"routeId\":20}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tripService);
    }
    @Test
    void post_semRouteId_deveRetornar400() throws Exception {
        mockMvc.perform(post("/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"busId\":10}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tripService);
    }
    @Test
    void post_quandoServiceLancaIllegalArgument_deveRetornar400ComErro() throws Exception {
        when(tripService.create(any(TripRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Ônibus não encontrado com o ID: 10"));

        mockMvc.perform(post("/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"busId\":10,\"routeId\":20}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ônibus não encontrado com o ID: 10"));
    }
    @Test
    void patchStatus_valido_deveChamarServiceComEnum() throws Exception {
        when(tripService.updateStatus(1L, TripStatus.COMPLETADA)).thenReturn(dto(1L, TripStatus.COMPLETADA));

        mockMvc.perform(patch("/trips/1/status").param("status", "COMPLETADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETADA"));

        verify(tripService).updateStatus(1L, TripStatus.COMPLETADA);
    }

    @Test
    void patchStatus_idInexistente_deveRetornar404() throws Exception {
        when(tripService.updateStatus(99L, TripStatus.CANCELADA))
                .thenThrow(new RuntimeException("Viagem não encontrada"));

        mockMvc.perform(patch("/trips/99/status").param("status", "CANCELADA"))
                .andExpect(status().isNotFound());
    }





}