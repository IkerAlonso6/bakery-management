package com.panaderia.service.impl;

import com.panaderia.domain.model.Contacto;
import com.panaderia.domain.model.Repartidor;
import com.panaderia.dto.request.RepartidorRequest;
import com.panaderia.dto.response.RepartidorResponse;
import com.panaderia.exception.RecursoNoEncontradoException;
import com.panaderia.repository.RepartidorRepository;
import com.panaderia.service.RepartidorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RepartidorServiceImpl implements RepartidorService {

    private final RepartidorRepository repartidorRepository;

    @Override
    public List<RepartidorResponse> listar() {
        return repartidorRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RepartidorResponse buscarPorId(Long id) {
        Repartidor repartidor = repartidorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Repartidor no encontrado"));
        return toResponse(repartidor);
    }

    @Override
    public RepartidorResponse crear(RepartidorRequest request) {
        Repartidor repartidor = Repartidor.builder()
                .nombre(request.getNombre())
                .build();
        Repartidor saved = repartidorRepository.save(repartidor);
        return toResponse(saved);
    }

    @Override
    public RepartidorResponse actualizar(Long id, RepartidorRequest request) {
        Repartidor repartidor = repartidorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Repartidor no encontrado"));
        repartidor.setNombre(request.getNombre());
        Repartidor saved = repartidorRepository.save(repartidor);
        return toResponse(saved);
    }

    private RepartidorResponse toResponse(Repartidor entity) {
        List<RepartidorResponse.ContactoDto> contactos = null;
        if (entity.getContactos() != null) {
            contactos = entity.getContactos().stream().map(this::toDto).collect(Collectors.toList());
        }
        return RepartidorResponse.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .contactos(contactos)
                .build();
    }

    private RepartidorResponse.ContactoDto toDto(Contacto contacto) {
        return RepartidorResponse.ContactoDto.builder()
                .id(contacto.getId())
                .valor(contacto.getValor())
                .tipoContacto(contacto.getTipoContacto() != null ? contacto.getTipoContacto().name() : null)
                .build();
    }
}
