package studing.studing_server.member.entity;

public enum MemberStatus {
    UNVERIFIED("ROLE_UNUSER"),    // 미인증 상태
    VERIFIED("ROLE_USER");        // 인증 완료 상태

    private final String role;

    MemberStatus(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
