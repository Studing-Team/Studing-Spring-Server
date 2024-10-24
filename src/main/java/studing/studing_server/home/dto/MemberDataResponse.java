package studing.studing_server.home.dto;

public record MemberDataResponse(
        Long admissionNumber,
        String name,
        String memberUniversity,
        String memberDepartment,
        String role
) {
}
