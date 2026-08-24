package alertbus.user_service.dto.response;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String email
) {
}
