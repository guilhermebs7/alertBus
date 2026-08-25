package alertbus.user_service.dto.request;

public record LoginRequestDTO(
        String email,
        String password
) {
}
