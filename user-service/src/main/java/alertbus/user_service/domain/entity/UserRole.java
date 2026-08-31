package alertbus.user_service.domain.entity;

public enum UserRole {
    ADMIN_COMPANY("ROLE_ADMIN_COMPANY"),
    DRIVER("ROLE_DRIVER"),
    PASSENGER("ROLE_PASSENGER");

    private String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
