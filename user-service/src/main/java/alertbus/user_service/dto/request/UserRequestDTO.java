package alertbus.user_service.dto.request;

public record UserRequestDTO(
        String name,
        String email,
        String password
) {
}
