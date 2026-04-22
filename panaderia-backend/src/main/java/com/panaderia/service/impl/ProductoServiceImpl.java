package com.panaderia.service.impl;

import com.panaderia.domain.model.Producto;
import com.panaderia.domain.enums.Unidad;
import com.panaderia.dto.request.ProductoRequest;
import com.panaderia.dto.response.ProductoResponse;
import com.panaderia.exception.RecursoNoEncontradoException;
import com.panaderia.exception.ReglaNegocioException;
import com.panaderia.repository.ProductoRepository;
import com.panaderia.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {
    private final ProductoRepository productoRepository;

    @Override
    public List<ProductoResponse> listarActivos() {
        return productoRepository.findByActivoTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductoResponse buscarPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        return toResponse(producto);
    }

    @Override
    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        if (productoRepository.existsByDescripcionAndUnidad(request.getDescripcion(), request.getUnidad())) {
            throw new ReglaNegocioException("Ya existe un producto con esa descripción y unidad");
        }
        Producto producto = Producto.builder()
                .descripcion(request.getDescripcion())
                .unidad(request.getUnidad())
                .activo(true)
                .build();
        productoRepository.save(producto);
        return toResponse(producto);
    }

    @Override
    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        if (!producto.getDescripcion().equals(request.getDescripcion()) || !producto.getUnidad().equals(request.getUnidad())) {
            if (productoRepository.existsByDescripcionAndUnidad(request.getDescripcion(), request.getUnidad())) {
                throw new ReglaNegocioException("Ya existe un producto con esa descripción y unidad");
            }
        }
        producto.setDescripcion(request.getDescripcion());
        producto.setUnidad(request.getUnidad());
        productoRepository.save(producto);
        return toResponse(producto);
    }

    @Override
    @Transactional
    public ProductoResponse desactivar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        producto.setActivo(false);
        productoRepository.save(producto);
        return toResponse(producto);
    }

    private ProductoResponse toResponse(Producto p) {
        return ProductoResponse.builder()
                .id(p.getId())
                .descripcion(p.getDescripcion())
                .unidad(p.getUnidad())
                .activo(p.isActivo())
                .build();
    }
}
