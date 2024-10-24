package studing.studing_server.home.dto;

public class LogoResponse {

    private final String universityLogoImage;
    private final String universityName;
    private final String collegeDepartmentLogoImage;
    private final String collegeDepartmentName;
    private final String departmentLogoImage;
    private final String departmentName;

    public LogoResponse(String universityLogoImage, String universityName,
                        String collegeDepartmentLogoImage, String collegeDepartmentName,
                        String departmentLogoImage, String departmentName) {
        this.universityLogoImage = universityLogoImage;
        this.universityName = universityName;
        this.collegeDepartmentLogoImage = collegeDepartmentLogoImage;
        this.collegeDepartmentName = collegeDepartmentName;
        this.departmentLogoImage = departmentLogoImage;
        this.departmentName = departmentName;
    }

    // Getters
    public String getUniversityLogoImage() {
        return universityLogoImage;
    }

    public String getUniversityName() {
        return universityName;
    }

    public String getCollegeDepartmentLogoImage() {
        return collegeDepartmentLogoImage;
    }

    public String getCollegeDepartmentName() {
        return collegeDepartmentName;
    }

    public String getDepartmentLogoImage() {
        return departmentLogoImage;
    }

    public String getDepartmentName() {
        return departmentName;
    }
}

