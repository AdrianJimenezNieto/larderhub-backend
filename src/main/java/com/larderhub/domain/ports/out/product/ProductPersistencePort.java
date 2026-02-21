package com.larderhub.domain.ports.out.product;

import com.larderhub.domain.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductPersistencePort {
  Product save(Product product);

  Optional<Product> findById(Long id);

  Optional<Product> findByBarcode(String barcode);

  List<Product> findAll();

  void deleteById(Long id);
}
