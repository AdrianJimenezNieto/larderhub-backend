package com.larderhub.infrastructure.adapter.out.persistence.user;

import com.larderhub.domain.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {
  UserEntity toEntity(User user);

  User toDomain(UserEntity entity);
}
