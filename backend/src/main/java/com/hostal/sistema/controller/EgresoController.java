package com.hostal.sistema.controller;

import com.hostal.sistema.dto.AnulacionRequest;
import com.hostal.sistema.dto.EgresoRequest;
import com.hostal.sistema.dto.EgresoResponse;
import com.hostal.sistema.exception.RecursoNoEncontradoException;
import com.hostal.sistema.repository.UsuarioRepository;
import com.hostal.sistema.service.EgresoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/egresos")
@RequiredArgsConstructor
@Tag(name = "Egresos", description = "Gestión de egresos del hostal")
public class EgresoController {

    private final EgresoService egresoService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping
    @Operation(summary = "Crear un egreso", description = "Registra un nuevo egreso en el sistema")
    public ResponseEntity<EgresoResponse> crear(@RequestBody @Valid EgresoRequest request) {
        Long userId = resolveUserId();
        return ResponseEntity.ok(egresoService.crear(userId, request));
    }

    @PostMapping("/{id}/anular")
    @Operation(summary = "Anular un egreso", description = "Anula un egreso existente con un motivo")
    public ResponseEntity<Void> anular(@PathVariable Long id, @RequestBody @Valid AnulacionRequest request) {
        Long userId = resolveUserId();
        egresoService.anular(userId, id, request.getMotivo());
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"))
                .getId();
    }
}
