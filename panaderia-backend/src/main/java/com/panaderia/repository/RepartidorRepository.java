package com.panaderia.repository;

import com.panaderia.domain.model.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepartidorRepository extends JpaRepository<Repartidor, Long> {
    // Ningún método especial por ahora
}
