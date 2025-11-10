package com.facushop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity // Le dice a JPA que esta clase es una tabla en la BD
@Table(name = "categories") // Nombra la tabla (buena práctica)
@Getter             // Lombok: Crea getters (ej. getId(), getName())
@Setter             // Lombok: Crea setters (ej. setName("..."))
@NoArgsConstructor  // Lombok: Crea un constructor vacío (requerido por JPA)
@AllArgsConstructor // Lombok: Crea un constructor con todos los campos
@Builder            // Lombok: Nos permite crear objetos de forma fluida
public class Category {

    @Id // Marca este campo como la Clave Primaria (Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental
    private Long id;

    @Column(nullable = false, unique = true) // No puede ser nulo y debe ser único
    private String name;

}
