package com.larderhub.infrastructure.adapter.in.rest.inventory;

import com.larderhub.domain.model.Product;
import com.larderhub.domain.ports.out.product.ProductPersistencePort;
import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.ProductCreateDTO;
import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.ProductResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductPersistencePort productPersistencePort;

  // POST /api/v1/products — Register a new product in the global catalog
  @PostMapping
  public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductCreateDTO dto) {
    // Check for duplicate barcode if provided
    if (dto.getBarcode() != null && !dto.getBarcode().isBlank()) {
      productPersistencePort.findByBarcode(dto.getBarcode()).ifPresent(p -> {
        throw new IllegalArgumentException("A product with this barcode already exists");
      });
    }

    Product product = Product.builder()
        .name(dto.getName())
        .barcode(dto.getBarcode())
        .imageUrl(dto.getImageUrl())
        .standardUnit(dto.getStandardUnit())
        .build();

    Product saved = productPersistencePort.save(product);
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(saved));
  }

  // GET /api/v1/products — List all products in the catalog
  @GetMapping
  public ResponseEntity<List<ProductResponseDTO>> listAll() {
    List<ProductResponseDTO> products = productPersistencePort.findAll().stream()
        .map(this::toResponseDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(products);
  }

  // GET /api/v1/products/{id} — Get a single product by ID
  @GetMapping("/{id}")
  public ResponseEntity<ProductResponseDTO> getById(@PathVariable Long id) {
    return productPersistencePort.findById(id)
        .map(p -> ResponseEntity.ok(toResponseDTO(p)))
        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
  }

  // DELETE /api/v1/products/{id} — Remove a product from the catalog
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (!productPersistencePort.findById(id).isPresent()) {
      throw new IllegalArgumentException("Product not found: " + id);
    }
    productPersistencePort.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  private ProductResponseDTO toResponseDTO(Product p) {
    return ProductResponseDTO.builder()
        .id(p.getId())
        .name(p.getName())
        .barcode(p.getBarcode())
        .imageUrl(p.getImageUrl())
        .standardUnit(p.getStandardUnit())
        .build();
  }
}
