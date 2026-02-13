package com.larderhub.infrastructure.adapter.out.persistence.product;

import com.larderhub.domain.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductPersistenceMapper {
  ProductEntity toEntity(Product product);

  Product toDomain(ProductEntity entity);
}
