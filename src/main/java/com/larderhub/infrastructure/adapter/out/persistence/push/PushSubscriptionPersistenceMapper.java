package com.larderhub.infrastructure.adapter.out.persistence.push;

import com.larderhub.domain.model.PushSubscription;
import com.larderhub.infrastructure.adapter.out.persistence.user.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PushSubscriptionPersistenceMapper {

  @Mapping(source = "userId", target = "user")
  PushSubscriptionEntity toEntity(PushSubscription domain);

  @Mapping(source = "user.id", target = "userId")
  PushSubscription toDomain(PushSubscriptionEntity entity);

  default UserEntity mapUserIdToEntity(Long id) {
    if (id == null) return null;
    return UserEntity.builder().id(id).build();
  }
}
