package com.hostal.sistema.controller;

import com.hostal.sistema.dto.AnulacionRequest;
import com.hostal.sistema.dto.IngresoRequest;
import com.hostal.sistema.dto.IngresoResponse;
import com.hostal.sistema.exception.RecursoNoEncontradoException;
import com.hostal.sistema.repository.UsuarioRepository;
import com.hostal.sistema.service.IngresoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingresos")
@RequiredArgsConstructor
@Tag(name = "Ingresos", description = "Gestión de ingresos del hostal")
public class IngresoController {

    private final IngresoService ingresoService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping
    @Operation(summary = "Crear un ingreso", description = "Registra un nuevo ingreso en el sistema")
    public ResponseEntity<IngresoResponse> crear(@RequestBody @Valid IngresoRequest request) {
        Long userId = resolveUserId();
        return ResponseEntity.ok(ingresoService.crear(userId, request));
    }

    @PostMapping("/{id}/anular")
    @Operation(summary = "Anular un ingreso", description = "Anula un ingreso existente con un motivo")
    public ResponseEntity<Void> anular(@PathVariable Long id, @RequestBody @Valid AnulacionRequest request) {
        Long userId = resolveUserId();
        ingresoService.anular(userId, id, request.getMotivo());
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"))
                .getId();
    }
}
