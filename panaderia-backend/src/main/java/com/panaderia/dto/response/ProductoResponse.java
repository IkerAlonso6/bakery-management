package com.panaderia.dto.response;

import com.panaderia.domain.enums.Unidad;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {
    private Long id;
    private String descripcion;
    private Unidad unidad;
    private boolean activo;
}
