package com.facushop.config;

import com.facushop.domain.Category;
import com.facushop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component // Le dice a Spring que gestione esta clase
@RequiredArgsConstructor // Lombok: Crea un constructor para los campos 'final'
public class DataInitializer implements CommandLineRunner {

    // Spring inyectará automáticamente el repositorio aquí
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        // Solo cargar datos si la tabla está vacía
        if (categoryRepository.count() == 0) {
            Category c1 = Category.builder().name("Indumentaria").build();
            Category c2 = Category.builder().name("Tecnologia").build();
            Category c3 = Category.builder().name("Hogar").build();
            Category c4 = Category.builder().name("Electrodomesticos").build();
            Category c5 = Category.builder().name("Belleza y Cuidado Personal").build();

            // Guardar todas las categorías en la base de datos
            categoryRepository.saveAll(List.of(c1, c2, c3, c4, c5));

            System.out.println(">>> 5 categorías iniciales han sido cargadas en la BD.");
        }
    }
}
