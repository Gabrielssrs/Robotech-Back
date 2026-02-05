package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ataques")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ataque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "arma_principal")
    private String armaPrincipal;

    @Column(name = "arma_secundaria")
    private String armaSecundaria;

    @Column
    private String alcance;

    @OneToOne
    @JoinColumn(name = "robot_id", unique = true)
    @JsonIgnore
    private Robot robot;
}