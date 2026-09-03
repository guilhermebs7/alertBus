package alertbus.route_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "tb_routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O código da rota é obrigatório")
    @Column(unique = true, nullable = false, length = 20)
    private String codigo;

    @NotBlank(message = "O nome da rota é obrigatório")
    @Column(nullable = false)
    private String nome;

    private String origem;

    private String destino;
}
