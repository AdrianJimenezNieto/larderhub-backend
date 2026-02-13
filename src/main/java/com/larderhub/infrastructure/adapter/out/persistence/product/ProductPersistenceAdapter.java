package com.larderhub.infrastructure.adapter.out.persistence.product;

import com.larderhub.domain.model.Product;
import com.larderhub.domain.ports.out.product.ProductPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductPersistencePort {

  private final ProductJpaRepository productJpaRepository;
  private final ProductPersistenceMapper productPersistenceMapper;

  @Override
  public Product save(Product product) {
    ProductEntity entity = productPersistenceMapper.toEntity(product);
    return productPersistenceMapper.toDomain(productJpaRepository.save(entity));
  }

  @Override
  public Optional<Product> findById(Long id) {
    return productJpaRepository.findById(id)
        .map(productPersistenceMapper::toDomain);
  }

  @Override
  public Optional<Product> findByBarcode(String barcode) {
    return productJpaRepository.findByBarcode(barcode)
        .map(productPersistenceMapper::toDomain);
  }
}
