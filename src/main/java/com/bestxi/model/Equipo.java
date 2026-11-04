package com.bestxi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "equipo")
@Getter
@Setter
@NoArgsConstructor
public class Equipo {

    @Id
    private Long id; // El id viene directamente de API-Football, no lo generamos nosotros

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "escudo_url")
    private String escudoUrl;
}
