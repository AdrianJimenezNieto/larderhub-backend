package com.larderhub.infrastructure.adapter.out.persistence.household;

import com.larderhub.domain.model.Household;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HouseholdPersistenceMapper {
  HouseholdEntity toEntity(Household household);

  Household toDomain(HouseholdEntity entity);
}
