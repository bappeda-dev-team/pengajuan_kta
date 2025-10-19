package cc.kertaskerja.pengajuan_kta.dto.Auth;

public class LoginResponse {
    private String access_token;

    public LoginResponse() {}

    public LoginResponse(String access_token) {
        this.access_token = access_token;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public static class Builder {
        private String access_token;

        public Builder access_token(String access_token) {
            this.access_token = access_token;
            return this;
        }

        public LoginResponse build() {
            return new LoginResponse(access_token);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
