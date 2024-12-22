package com.example.Semana7MascotasRM.service;

import com.example.Semana7MascotasRM.model.Mascota;
import com.example.Semana7MascotasRM.repository.MascotaRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MascotaService {

    private final MascotaRepository repository;

    public MascotaService(MascotaRepository repository) {
        this.repository = repository;
    }

    // Guarda o actualiza una mascota en la base de datos
    public void guardarMascota(Mascota mascota) {
        repository.save(mascota);
    }

    // Lista todas las mascotas registradas
    public Iterable<Mascota> listarMascotas() {
        return repository.findAll();
    }

    // Busca una mascota por su ID
    public Optional<Mascota> buscarMascotaPorId(Long id) {  // Cambiado de String a Long
        return repository.findById(id);
    }

    // Elimina una mascota por su ID
    public void eliminarMascota(Long id) {  // Cambiado de String a Long
        repository.deleteById(id);
    }
}