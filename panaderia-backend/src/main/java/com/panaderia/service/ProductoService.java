package com.panaderia.service;

import com.panaderia.dto.request.ProductoRequest;
import com.panaderia.dto.response.ProductoResponse;

import java.util.List;

public interface ProductoService {
    List<ProductoResponse> listarActivos();
    ProductoResponse buscarPorId(Long id);
    ProductoResponse crear(ProductoRequest request);
    ProductoResponse actualizar(Long id, ProductoRequest request);
    ProductoResponse desactivar(Long id);
}
