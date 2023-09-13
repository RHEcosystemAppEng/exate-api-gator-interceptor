package exate.gator.interceptor.api;

public record TokenPayload(String clientId, String clientSecret, String grantType) {
    @Override
    public String toString() {
        return String.format("client_id=%s&client_secret=%s&grant_type=%s", clientId, clientSecret, grantType);
    }
}
