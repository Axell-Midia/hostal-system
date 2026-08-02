package com.hostal.sistema.service;

import com.hostal.sistema.config.JwtUtil;
import com.hostal.sistema.dto.LoginRequest;
import com.hostal.sistema.dto.LoginResponse;
import com.hostal.sistema.dto.UserInfoResponse;
import com.hostal.sistema.exception.RecursoNoEncontradoException;
import com.hostal.sistema.model.Usuario;
import com.hostal.sistema.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        String token = jwtUtil.generateToken(usuario.getUsername());

        return new LoginResponse(
                token,
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getSede().getNombre()
        );
    }

    public UserInfoResponse getUserInfo(Long userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        return new UserInfoResponse(
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getSede().getNombre(),
                usuario.getSede().getId()
        );
    }
}
