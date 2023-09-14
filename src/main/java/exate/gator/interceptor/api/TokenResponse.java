package exate.gator.interceptor.api;

/** Record deserializing API-Gator TOKEN responses. */
public record TokenResponse(String access_token, int expires_in, String token_type, String scope, String refresh_token) {}
