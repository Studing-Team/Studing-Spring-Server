package studing.studing_server.member.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
) {}