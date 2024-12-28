package studing.studing_server.member.dto;

public record ChangePasswordRequest(
        String loginIdentifier,
        String currentPassword,
        String newPassword
) {}