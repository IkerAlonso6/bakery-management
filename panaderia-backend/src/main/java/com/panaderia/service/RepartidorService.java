package com.panaderia.service;

import com.panaderia.dto.request.RepartidorRequest;
import com.panaderia.dto.response.RepartidorResponse;

import java.util.List;

public interface RepartidorService {
    List<RepartidorResponse> listar();
    RepartidorResponse buscarPorId(Long id);
    RepartidorResponse crear(RepartidorRequest request);
    RepartidorResponse actualizar(Long id, RepartidorRequest request);
}
