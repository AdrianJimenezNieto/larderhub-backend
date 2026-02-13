package com.larderhub.infrastructure.adapter.out.persistence.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {
  Optional<ProductEntity> findByBarcode(String barcode);
}
