package com.larderhub.infrastructure.adapter.out.persistence.shoppingList;

import com.larderhub.domain.model.ShoppingListItem;
import com.larderhub.infrastructure.adapter.out.persistence.household.HouseholdEntity;
import com.larderhub.infrastructure.adapter.out.persistence.product.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShoppingListItemPersistenceMapper {

  @Mapping(source = "householdId", target = "household")
  @Mapping(source = "productId", target = "product")
  @Mapping(source = "checked", target = "isChecked")
  ShoppingListItemEntity toEntity(ShoppingListItem domain);

  @Mapping(source = "household.id", target = "householdId")
  @Mapping(source = "product.id", target = "productId")
  @Mapping(source = "checked", target = "isChecked")
  ShoppingListItem toDomain(ShoppingListItemEntity entity);

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
