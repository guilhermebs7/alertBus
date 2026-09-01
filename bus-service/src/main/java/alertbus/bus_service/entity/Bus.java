package alertbus.bus_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "tb_buses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bus {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank(message = "A placa é obrigatória")
    @Column(unique = true, nullable = false, length = 10)
    private String placa;

    private String modelo;
    private Integer capacidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusStatus status;

}
