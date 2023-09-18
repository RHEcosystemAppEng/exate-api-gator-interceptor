package exate.gator.interceptor.content;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Record for serializing the payload for sending TOKEN requests to API-Gator.
 * Use the toString method for FORMS content.
 */
public record TokenPayload(String clientId, String clientSecret, String grantType) {
    /** Returns the string representation of the request body for usage with FORM-like content types */
    public TokenPayload(String clientId, String clientSecret, String grantType) {
        this.clientId = URLEncoder.encode(clientId, StandardCharsets.UTF_8);
        this.clientSecret = URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);
        this.grantType = URLEncoder.encode(grantType, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return String.format("client_id=%s&client_secret=%s&grant_type=%s", clientId, clientSecret, grantType);
    }
}
