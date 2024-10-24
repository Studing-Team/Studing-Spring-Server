package studing.studing_server.home.dto;

public record LogoResponse(
        String universityLogoImage,
        String universityName,
        String collegeDepartmentLogoImage,
        String collegeDepartmentName,
        String departmentLogoImage,
        String departmentName
) {

}
