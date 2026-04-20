package com.panaderia.domain.model;

import com.panaderia.domain.enums.TipoContacto;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "contacto")
public class Contacto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoContacto tipoContacto;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "repartidor_id")
    private Repartidor repartidor;
}