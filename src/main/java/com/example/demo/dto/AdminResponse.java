package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse {
    private Long id;
    private String nombre;
    private String dni;
    private String telefono;
    private String correo;
    @JsonProperty("isEnabled")
    private boolean isEnabled;
}