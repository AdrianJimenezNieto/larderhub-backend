package com.larderhub.domain.ports.in.recipe;

import com.larderhub.infrastructure.adapter.in.rest.recipe.dto.RecipeCreateDTO;
import com.larderhub.infrastructure.adapter.in.rest.recipe.dto.RecipeResponseDTO;
import com.larderhub.infrastructure.adapter.in.rest.recipe.dto.RecipeSuggestionDTO;
import com.larderhub.infrastructure.adapter.in.rest.recipe.dto.UpdateRecipeDTO;

import java.util.List;

public interface RecipeUseCase {

  RecipeResponseDTO createRecipe(RecipeCreateDTO dto, String username);

  List<RecipeResponseDTO> getAllRecipes();

  RecipeResponseDTO getRecipeById(Long id);

  void deleteRecipe(Long id, String username);

  RecipeResponseDTO updateRecipe(Long id, UpdateRecipeDTO dto, String username);

  // Social
  RecipeResponseDTO rateRecipe(Long recipeId, Integer rating, String comment, String username);

  // Cross-Domain Engine
  List<RecipeSuggestionDTO> getSuggestionsForHousehold(Long householdId, String username);

  int addMissingToShoppingList(Long householdId, Long recipeId, String username);
}
