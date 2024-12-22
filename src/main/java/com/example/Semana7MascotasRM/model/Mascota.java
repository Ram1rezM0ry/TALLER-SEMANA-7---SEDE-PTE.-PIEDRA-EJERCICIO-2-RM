package com.example.Semana7MascotasRM.model;

import jakarta.persistence.*;
import java.util.Base64;
import org.springframework.web.multipart.MultipartFile;

@Entity
@Table(name = "mascotas")
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Esto indica que el id es auto-incrementable
    private Long id; // Cambiado a Long o Integer según el tipo en la base de datos

    private String nombre;

    private Integer edad;

    private String especie;

    private String raza;

    @Lob
    private byte[] imagen;


    @Transient
    private MultipartFile archivoImagen;

    // Getters y Setters

    public Long getId() {  
        return id;
    }

    public void setId(Long id) {  
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public MultipartFile getArchivoImagen() {
        return archivoImagen;
    }

    public void setArchivoImagen(MultipartFile archivoImagen) {
        this.archivoImagen = archivoImagen;
    }

    // Método para convertir la imagen a Base64
    public String getImagenBase64() {
        if (imagen != null) {
            return Base64.getEncoder().encodeToString(imagen);
        }
        return null;
    }
}