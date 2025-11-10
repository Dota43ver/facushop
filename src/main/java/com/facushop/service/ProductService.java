package com.facushop.service;

import com.facushop.domain.Product;
import com.facushop.dto.ProductRequest;
import com.facushop.repository.CategoryRepository;
import com.facushop.repository.ProductRepository;
import com.facushop.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import com.facushop.domain.User;
import com.facushop.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import com.facushop.dto.response.CategoryDto;
import com.facushop.dto.response.ProductResponseDto;
import com.facushop.dto.response.SellerDto;
import java.util.stream.Collectors;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StorageService storageService; // Inyectamos el servicio de bucket
    private final UserRepository userRepository;

    private ProductResponseDto mapToProductDto(Product product) {
        // 1. Mapear la Categoría
        CategoryDto categoryDto = CategoryDto.builder()
                .id(product.getCategory().getId())
                .name(product.getCategory().getName())
                .build();

        // 2. Mapear el Vendedor (Seller)
        SellerDto sellerDto = SellerDto.builder()
                .firstname(product.getSeller().getFirstname())
                .lastname(product.getSeller().getLastname())
                .email(product.getSeller().getEmail())
                .build();

        // 3. Mapear el Producto
        return ProductResponseDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .category(categoryDto)
                .seller(sellerDto)
                .build();
    }

    public ProductResponseDto createProduct(ProductRequest request, MultipartFile imageFile, UserDetails loggedInUser) throws IOException {

        // 1. Buscar al vendedor (el usuario logueado)
        User seller = userRepository.findByEmail(loggedInUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Subir la imagen
        String imageUrl = storageService.uploadFile(imageFile);

        // 3. Buscar la categoría
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // 4. Crear el objeto Producto
        Product product = Product.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(imageUrl)
                .category(category)
                .seller(seller) // <-- ¡ASIGNA EL VENDEDOR!
                .build();

        // 5. Guardar en la BD y devolver
        Product savedProduct = productRepository.save(product);

        return mapToProductDto(savedProduct);
    }

    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToProductDto) // Convierte cada producto a DTO
                .collect(Collectors.toList());
    }

    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        return mapToProductDto(product);
    }

    public List<ProductResponseDto> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToProductDto)
                .collect(Collectors.toList());
    }

    public ProductResponseDto updateProduct(Long id, ProductRequest request, MultipartFile imageFile, UserDetails loggedInUser) throws IOException {
        // 1. Validar que el usuario es el dueño
        Product productToUpdate = validateOwnership(id, loggedInUser);

        // 2. Buscar la categoría
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // 3. Actualizar campos de texto
        productToUpdate.setTitle(request.getTitle());
        productToUpdate.setDescription(request.getDescription());
        productToUpdate.setPrice(request.getPrice());
        productToUpdate.setCategory(category);

        // 4. (Opcional) Si mandan una nueva imagen, la actualizamos
        if (imageFile != null && !imageFile.isEmpty()) {
            // (Futura Tarea: Borrar la imagen vieja del bucket)
            // storageService.deleteFile(productToUpdate.getImageUrl());

            String newImageUrl = storageService.uploadFile(imageFile);
            productToUpdate.setImageUrl(newImageUrl);
        }

        // 5. Guardar y devolver
        Product updatedProduct = productRepository.save(productToUpdate);

        return mapToProductDto(updatedProduct);
    }
    public void deleteProduct(Long id, UserDetails loggedInUser) {
        // 1. Validar que el usuario es el dueño
        Product productToDelete = validateOwnership(id, loggedInUser);

        // (Futura Tarea: Borrar la imagen del bucket)
        // storageService.deleteFile(productToDelete.getImageUrl());

        // 2. Borrar de la BD
        productRepository.delete(productToDelete);
    }

    private Product validateOwnership(Long productId, UserDetails loggedInUser) {
        // 1. Buscar el producto (usamos el método optimizado)
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + productId));

        // 2. Obtener el email del usuario logueado
        String loggedInEmail = loggedInUser.getUsername();

        // 3. Obtener el email del dueño del producto
        String ownerEmail = product.getSeller().getEmail();

        // 4. ¡LA VALIDACIÓN!
        if (!loggedInEmail.equals(ownerEmail)) {
            throw new AccessDeniedException("No tienes permiso para modificar este producto.");
        }

        // 5. Si todo OK, devuelve el producto
        return product;
    }
}
