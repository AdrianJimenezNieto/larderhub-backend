package com.larderhub.infrastructure.adapter.out.persistence.shopping;

import com.larderhub.domain.model.ShoppingItem;
import com.larderhub.infrastructure.adapter.out.persistence.household.HouseholdEntity;
import com.larderhub.infrastructure.adapter.out.persistence.product.ProductEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShoppingItemPersistenceMapper {

  // Map domain → JPA entity (householdId/productId →
  // HouseholdEntity/ProductEntity stubs)
  @Mapping(target = "household", expression = "java(householdStub(item.getHouseholdId()))")
  @Mapping(target = "product", expression = "java(productStub(item.getProductId()))")
  ShoppingItemEntity toEntity(ShoppingItem item);

  // Map JPA entity → domain (extract IDs from nested entities)
  @Mapping(target = "householdId", source = "household.id")
  @Mapping(target = "productId", source = "product.id")
  ShoppingItem toDomain(ShoppingItemEntity entity);

  // Create a minimal HouseholdEntity stub (only id needed for FK)
  default HouseholdEntity householdStub(Long householdId) {
    HouseholdEntity e = new HouseholdEntity();
    e.setId(householdId);
    return e;
  }

  // Create a minimal ProductEntity stub (only id needed for FK)
  default ProductEntity productStub(Long productId) {
    ProductEntity e = new ProductEntity();
    e.setId(productId);
    return e;
  }
}
