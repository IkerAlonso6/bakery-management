package com.panaderia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepartidorResponse {
    private Long id;
    private String nombre;
    private List<ContactoDto> contactos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactoDto {
        private Long id;
        private String valor;
        private String tipoContacto;
    }
}
