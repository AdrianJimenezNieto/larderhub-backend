package com.larderhub.infrastructure.adapter.out.persistence.recipe;

import com.larderhub.domain.model.Recipe;
import com.larderhub.infrastructure.adapter.out.persistence.user.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecipePersistenceMapper {

  @Mapping(source = "authorId", target = "author")
  RecipeEntity toEntity(Recipe domain);

  @Mapping(source = "author.id", target = "authorId")
  Recipe toDomain(RecipeEntity entity);

  default UserEntity mapAuthorIdToEntity(Long id) {
    if (id == null)
      return null;
    return UserEntity.builder().id(id).build();
  }
}
