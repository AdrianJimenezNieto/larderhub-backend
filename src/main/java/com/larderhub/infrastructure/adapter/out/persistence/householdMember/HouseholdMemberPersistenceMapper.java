package com.larderhub.infrastructure.adapter.out.persistence.householdMember;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.larderhub.domain.model.HouseholdMember;
import com.larderhub.infrastructure.adapter.out.persistence.household.HouseholdEntity;
import com.larderhub.infrastructure.adapter.out.persistence.user.UserEntity;

@Mapper(componentModel = "spring")
public interface HouseholdMemberPersistenceMapper {

  // Domain to Entity
  @Mapping(target = "id", ignore = true) // id is managed by JPA (@GeneratedValue)
  @Mapping(source = "userId", target = "user")
  @Mapping(source = "householdId", target = "household")
  HouseholdMemberEntity toEntity(HouseholdMember householdMember);

  // Entity to Domain
  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "householdId", source = "household.id")
  HouseholdMember toDomain(HouseholdMemberEntity householdMemberEntity);

  // Helper methods
  default UserEntity mapUserIdToEntity(Long userId) {
    if (userId == null)
      return null;
    return UserEntity.builder().id(userId).build();
  }

  default HouseholdEntity mapHouseholdIdToEntity(Long householdId) {
    if (householdId == null)
      return null;
    return HouseholdEntity.builder().id(householdId).build();
  }
}
