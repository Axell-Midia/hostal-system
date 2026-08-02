package com.hostal.sistema.controller;

import com.hostal.sistema.dto.LoginRequest;
import com.hostal.sistema.dto.LoginResponse;
import com.hostal.sistema.dto.UserInfoResponse;
import com.hostal.sistema.repository.UsuarioRepository;
import com.hostal.sistema.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de login y información de usuario")
public class AuthController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y retorna un token JWT")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener información del usuario autenticado")
    public ResponseEntity<UserInfoResponse> getUserInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new com.hostal.sistema.exception.RecursoNoEncontradoException("Usuario no encontrado"))
                .getId();
        return ResponseEntity.ok(authService.getUserInfo(userId));
    }
}
