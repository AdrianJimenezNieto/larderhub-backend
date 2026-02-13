package com.larderhub.infrastructure.adapter.out.persistence.pantry;

import com.larderhub.domain.model.PantryItem;
import com.larderhub.infrastructure.adapter.out.persistence.household.HouseholdEntity;
import com.larderhub.infrastructure.adapter.out.persistence.product.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PantryItemPersistenceMapper {

  @Mapping(source = "householdId", target = "household")
  @Mapping(source = "productId", target = "product")
  PantryItemEntity toEntity(PantryItem domain);

  @Mapping(source = "household.id", target = "householdId")
  @Mapping(source = "product.id", target = "productId")
  PantryItem toDomain(PantryItemEntity entity);

  default HouseholdEntity mapHouseholdIdToEntity(Long id) {
    if (id == null)
      return null;
    return HouseholdEntity.builder().id(id).build();
  }

  default ProductEntity mapProductIdToEntity(Long id) {
    if (id == null)
      return null;
    return ProductEntity.builder().id(id).build();
  }
}
