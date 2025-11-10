package com.facushop.controller;


import com.facushop.dto.ProductRequest;
import com.facushop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import com.facushop.dto.response.ProductResponseDto;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

@RestController
@RequestMapping("/api/products") // Ruta base (protegida por defecto)
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ProductResponseDto> createProduct(
            @ModelAttribute ProductRequest request,
            @RequestPart("image") MultipartFile imageFile,
            @AuthenticationPrincipal UserDetails loggedInUser // <-- ¡AÑADE ESTO!
    ) {
        try {
            // Pasamos el usuario al servicio
            ProductResponseDto createdProduct = productService.createProduct(request, imageFile, loggedInUser);
            return ResponseEntity.ok(createdProduct);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }


    @GetMapping("/category/{id}")
    public ResponseEntity<List<ProductResponseDto>> getProductsByCategory(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductsByCategory(id));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @ModelAttribute ProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile, // La imagen es opcional
            @AuthenticationPrincipal UserDetails loggedInUser
    ) {
        try {
            ProductResponseDto updatedProduct = productService.updateProduct(id, request, imageFile, loggedInUser);
            return ResponseEntity.ok(updatedProduct);
        } catch (AccessDeniedException e) {
            // Si el servicio lanza el error, devolvemos 403 Forbidden
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) { // Ej. Producto o Categoría no encontrada
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) { // Error de subida de imagen
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails loggedInUser
    ) {
        try {
            productService.deleteProduct(id, loggedInUser);
            return ResponseEntity.noContent().build(); // 204 No Content (éxito)
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
