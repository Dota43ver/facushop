package com.facushop.repository;

import com.facushop.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Le dice a Spring que esto es un componente de acceso a datos
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // ¡Eso es todo!
    // JpaRepository nos da automáticamente:
    // - save()
    // - findById()
    // - findAll()
    // - delete()
    // - ...y muchos más.
}
