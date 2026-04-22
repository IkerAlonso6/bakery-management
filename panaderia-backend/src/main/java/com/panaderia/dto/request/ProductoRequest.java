package com.panaderia.dto.request;

import com.panaderia.domain.enums.Unidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequest {
    @NotBlank
    private String descripcion;

    @NotNull
    private Unidad unidad;
}
