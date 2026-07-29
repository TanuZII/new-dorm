package th.ac.dusit.dorm.identity;

public record UserResponse(
        Long id,
        String username,
        String displayName,
        String email,
        UserRole role,
        boolean active) {

    static UserResponse from(AppUserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.getRole(),
                user.isActive());
    }
}
