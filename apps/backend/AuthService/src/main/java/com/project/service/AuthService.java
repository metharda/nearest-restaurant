package com.project.service;

import org.springframework.stereotype.Service;
import com.project.dto.request.RegisterRequestDto;
import com.project.dto.request.LoginRequestDto;
import com.project.dto.response.RegisterResponseDto;
import com.project.dto.response.UserResponseDto;
import com.project.entity.Auth;
import com.project.repository.AuthRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthRepository repository;

    public RegisterResponseDto register(RegisterRequestDto dto) {
        if(!repository.existsByUsername(dto.getUsername())){
            Auth auth = repository.save(
                new Auth(
                    repository.get_current_id(), 
                    dto.getUsername(), 
                    dto.getPassword(), 
                    dto.getEmail()
                )
            );
            return new RegisterResponseDto(
                auth.getUsername(), 
                auth.getEmail(), 
                auth.getPassword()
            );
        }
        return null;
    }

    public Boolean login(LoginRequestDto dto) {
        return repository.existsByUsernameAndPassword(dto.getUsername(), dto.getPassword());
    }

    public int findUserIDbyUsername(LoginRequestDto dto) {
        return repository.fetch_userid_by_username(dto.getUsername());
    }

    public UserResponseDto findUserbyUserID(int user_id) {
        Auth auth = repository.fetch_user_by_userid(user_id);
        return new UserResponseDto(auth.getUsername(), auth.getEmail());
    }
    
}
