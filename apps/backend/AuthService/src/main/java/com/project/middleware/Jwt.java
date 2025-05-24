package com.project.middleware;

import java.util.Date;
import com.project.datastructures.HashMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import io.github.cdimascio.dotenv.Dotenv;

public class Jwt {
    private static final String SECRET_KEY = Dotenv.load().get("SECRET_KEY");
    private static final long EXPIRATION_TIME = 3600000;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static long getExp(){
        return EXPIRATION_TIME;
    }

    public static String generateToken(String userid) {
        String token = "";
        try {
            ClaimsBuilder c_builder = Jwts.claims();
            c_builder.add("expDate", new Date(System.currentTimeMillis() + EXPIRATION_TIME).toString());
            c_builder.subject(userid);
            Claims claims = c_builder.build();
            String header = encodeBase64Url("{\"alg\": \"HS256\", \"typ\": \"JWT\"}");
            String encodedPayload = encodeBase64Url(objectMapper.writeValueAsString(claims));
            String signature = generateSignature(
                header, 
                encodedPayload, 
                encodeBase64Url(SECRET_KEY)
            );
            token = header + "." + encodedPayload + "." + signature;
        }
        catch (Exception e) {
            return "";
        }
        return token;
    }

    public static String validateToken(String token) {
        int user_id;
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException();
            }

            String payload = decodeBase64Url(parts[1]);
            String signature = parts[2];
            String expectedSignature = generateSignature(parts[0], parts[1], encodeBase64Url(SECRET_KEY));

            if (!signature.equals(expectedSignature)) {
                throw new IllegalArgumentException();
            }
            HashMap<String, Object> map = (HashMap<String, Object>) objectMapper.readValue(payload, new TypeReference<HashMap<String, Object>>() {});
            user_id = Integer.parseInt(map.get("sub").toString());
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return String.valueOf(user_id);
    }

    public static String decodeBase64Url(String data) {
        return new String(Base64.getUrlDecoder().decode(data), StandardCharsets.UTF_8);
    }

    public static String encodeBase64Url(String data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateSignature(String header, String payload, String secret) throws Exception {
        String data = header + "." + payload;
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
    }
}
