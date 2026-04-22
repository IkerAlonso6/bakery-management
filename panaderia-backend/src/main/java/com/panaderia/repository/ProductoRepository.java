package com.panaderia.repository;

import com.panaderia.domain.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    java.util.List<Producto> findByActivoTrue();
    boolean existsByDescripcionAndUnidad(String descripcion, com.panaderia.domain.enums.Unidad unidad);
}
