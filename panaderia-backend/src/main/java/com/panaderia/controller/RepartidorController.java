package com.panaderia.controller;

import com.panaderia.dto.request.RepartidorRequest;
import com.panaderia.dto.response.RepartidorResponse;
import com.panaderia.service.RepartidorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repartidores")
@RequiredArgsConstructor
public class RepartidorController {

    private final RepartidorService repartidorService;

    @GetMapping
    public ResponseEntity<List<RepartidorResponse>> listar() {
        return ResponseEntity.ok(repartidorService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepartidorResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(repartidorService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<RepartidorResponse> crear(@Valid @RequestBody RepartidorRequest request) {
        return ResponseEntity.ok(repartidorService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RepartidorResponse> actualizar(@PathVariable Long id, @Valid @RequestBody RepartidorRequest request) {
        return ResponseEntity.ok(repartidorService.actualizar(id, request));
    }
}
