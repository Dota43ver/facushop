package com.facushop.repository;

import com.facushop.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Override
    @EntityGraph(attributePaths = {"category", "seller"}) // Carga categoría Y vendedor
    List<Product> findAll();

    @Override
    @EntityGraph(attributePaths = {"category", "seller"}) // Carga categoría Y vendedor
    Optional<Product> findById(Long id);

    @EntityGraph(attributePaths = {"category", "seller"}) // Carga categoría Y vendedor
    List<Product> findByCategoryId(Long categoryId);
}
