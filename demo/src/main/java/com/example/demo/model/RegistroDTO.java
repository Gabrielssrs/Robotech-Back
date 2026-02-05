package com.example.demo.model;

import jakarta.validation.constraints.*;

public class RegistroDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = "^[A-Z][a-z]*$", message = "El nombre debe empezar con mayúscula y contener solo letras")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Pattern(regexp = "^[A-Z][a-z]*( [A-Z][a-z]*)*$", message = "El apellido debe empezar con mayúscula y contener solo letras (permitir espacios)")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^\\d{8,15}$", message = "El DNI debe contener solo números y tener entre 8 y 15 dígitos")
    private String dni;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\d{8,15}$", message = "El teléfono debe contener solo números y tener entre 8 y 15 dígitos")
    private String telefono;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
             message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial")
    private String password;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El club es obligatorio")
    private String club;

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getClub() {
        return club;
    }

    public void setClub(String club) {
        this.club = club;
    }

    // Método de validación cruzada
    public boolean dniIgualAContrasena() {
        return dni != null && dni.equals(password);
    }

    // Método para validar todas las reglas de negocio
    public void validarReglasNegocio() throws IllegalArgumentException {
        if (dniIgualAContrasena()) {
            throw new IllegalArgumentException("La contraseña no puede ser igual al DNI");
        }
        // Aquí puedes agregar más validaciones cruzadas si es necesario
    }
}