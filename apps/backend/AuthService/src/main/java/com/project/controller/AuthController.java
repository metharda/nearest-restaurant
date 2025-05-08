package com.project.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.dto.request.RegisterRequestDto;
import com.project.dto.request.LoginRequestDto;
import com.project.dto.response.RegisterResponseDto;
import com.project.dto.response.LoginResponseDto;
import com.project.dto.response.UserResponseDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.project.service.AuthService;
import lombok.RequiredArgsConstructor;
import com.project.middleware.Jwt;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class AuthController {
    private final AuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/register")
    public ResponseEntity<?> register(HttpEntity<String> request) {
        RegisterResponseDto dto_resp;
        try{
            String body = request.getBody();
            JsonNode jsonNode = objectMapper.readTree(body);
            String username = jsonNode.get("username").asText();
            String password = jsonNode.get("password").asText();
            String repassword = jsonNode.get("repassword").asText();
            String email = jsonNode.get("email").asText();
            if(!password.equals(repassword)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Passwords are wrong");
            }
            RegisterRequestDto dto_req = new RegisterRequestDto(username, password, repassword, email);
            dto_resp = authService.register(dto_req);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error");
        }
        if(dto_resp != null){
            return ResponseEntity.ok(dto_resp);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Null Response");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpEntity<String> request) {
        LoginResponseDto dto_resp;
        String token = "";
        try{
            String body = request.getBody();
            JsonNode jsonNode = objectMapper.readTree(body);
            String username = jsonNode.get("username").asText();
            String password = jsonNode.get("password").asText();
            LoginRequestDto dto_req = new LoginRequestDto(username, password);
            if(authService.login(dto_req)){
                token = Jwt.generateToken(String.valueOf(authService.findUserIDbyUsername(dto_req)));
            }
            if(token.equals("")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username or Password is wrong");
            }
            dto_resp = new LoginResponseDto(token, Jwt.getExp());
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e);
        }
        return ResponseEntity.ok(dto_resp);
    }

    @GetMapping("/user")
    public ResponseEntity<?> get_user(HttpEntity<String> request) {
        UserResponseDto dto;
        try {
            String header = request.getHeaders().getFirst("Authorization");
            if(header == null) {
                throw new IllegalArgumentException();
            }
            String token = header.replace("Bearer ", "");
            int user_id = Integer.parseInt(Jwt.validateToken(token));
            dto = authService.findUserbyUserID(user_id);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Null Response");
        }
        return ResponseEntity.ok(dto); 
    }
}
