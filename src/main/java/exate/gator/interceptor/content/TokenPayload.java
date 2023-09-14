package exate.gator.interceptor.content;

/**
 * Record for serializing the payload for sending TOKEN requests to API-Gator.
 * Use the toString method for FORMS content.
 */
public record TokenPayload(String clientId, String clientSecret, String grantType) {
    /** Returns the string representation of the request body for usage with FORM-like content types */
    @Override
    public String toString() {
        return String.format("client_id=%s&client_secret=%s&grant_type=%s", clientId, clientSecret, grantType);
    }
}
