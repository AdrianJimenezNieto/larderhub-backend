package com.larderhub.application;

import com.larderhub.domain.model.PantryItem;
import com.larderhub.domain.model.Product;
import com.larderhub.domain.model.ShoppingItem;
import com.larderhub.domain.model.Recipe;
import com.larderhub.domain.model.RecipeIngredient;
import com.larderhub.domain.model.RecipeRating;
import com.larderhub.domain.model.RecipeStep;
import com.larderhub.domain.model.User;
import com.larderhub.domain.ports.in.recipe.RecipeUseCase;
import com.larderhub.domain.ports.out.householdMembers.HouseholdMembersPersistencePort;
import com.larderhub.domain.ports.out.pantry.PantryItemPersistencePort;
import com.larderhub.domain.ports.out.product.ProductPersistencePort;
import com.larderhub.domain.ports.out.recipe.RecipePersistencePort;
import com.larderhub.domain.ports.out.recipe.RecipeRatingPersistencePort;
import com.larderhub.domain.ports.out.shopping.ShoppingItemPersistencePort;
import com.larderhub.domain.ports.out.user.UserPersistencePort;
import com.larderhub.infrastructure.adapter.in.rest.recipe.dto.RecipeCreateDTO;
import com.larderhub.infrastructure.adapter.in.rest.recipe.dto.RecipeResponseDTO;
import com.larderhub.infrastructure.adapter.in.rest.recipe.dto.RecipeSuggestionDTO;
import com.larderhub.infrastructure.adapter.in.rest.recipe.dto.UpdateRecipeDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService implements RecipeUseCase {

  private final RecipePersistencePort recipePersistencePort;
  private final RecipeRatingPersistencePort ratingPersistencePort;
  private final ProductPersistencePort productPersistencePort;
  private final UserPersistencePort userPersistencePort;

  private final PantryItemPersistencePort pantryItemPersistencePort;
  private final HouseholdMembersPersistencePort householdMembersPersistencePort;
  private final ShoppingItemPersistencePort shoppingItemPersistencePort;

  @Override
  public RecipeResponseDTO createRecipe(RecipeCreateDTO dto, String username) {
    User author = userPersistencePort.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

    Recipe recipe = Recipe.builder()
        .title(dto.getTitle())
        .description(dto.getDescription())
        .imageUrl(dto.getImageUrl())
        .authorId(author.getId())
        .cookingTimeMinutes(dto.getCookingTimeMinutes())
        .difficulty(dto.getDifficulty())
        .servings(dto.getServings())
        .build();

    List<RecipeIngredient> ingredients = dto.getIngredients().stream().map(ing -> {
      if (productPersistencePort.findById(ing.getProductId()).isEmpty()) {
        throw new IllegalArgumentException("Product not found: " + ing.getProductId());
      }
      return RecipeIngredient.builder()
          .productId(ing.getProductId())
          .quantity(ing.getQuantity())
          .unit(ing.getUnit())
          .build();
    }).collect(Collectors.toList());
    recipe.setIngredients(ingredients);

    List<RecipeStep> steps = dto.getSteps().stream().map(step -> RecipeStep.builder()
        .stepNumber(step.getStepNumber())
        .title(step.getTitle())
        .description(step.getDescription())
        .imageUrl(step.getImageUrl())
        .timerMinutes(step.getTimerMinutes())
        .build()).collect(Collectors.toList());
    recipe.setSteps(steps);

    Recipe saved = recipePersistencePort.save(recipe);
    return inflateResponse(saved);
  }

  @Override
  public List<RecipeResponseDTO> getAllRecipes() {
    return recipePersistencePort.findAll().stream()
        .map(this::inflateResponse)
        .collect(Collectors.toList());
  }

  @Override
  public RecipeResponseDTO getRecipeById(Long id) {
    Recipe recipe = recipePersistencePort.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + id));
    return inflateResponse(recipe);
  }

  @Override
  public RecipeResponseDTO updateRecipe(Long id, UpdateRecipeDTO dto, String username) {
    Recipe recipe = recipePersistencePort.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + id));

    User user = userPersistencePort.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!recipe.getAuthorId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
      throw new AccessDeniedException("Solo el autor puede editar esta receta");
    }

    recipe.setTitle(dto.getTitle());
    recipe.setDescription(dto.getDescription());
    recipe.setImageUrl(dto.getImageUrl());
    recipe.setCookingTimeMinutes(dto.getCookingTimeMinutes());
    recipe.setDifficulty(dto.getDifficulty());
    recipe.setServings(dto.getServings());

    recipe.setIngredients(dto.getIngredients().stream().map(ing -> {
      if (productPersistencePort.findById(ing.getProductId()).isEmpty()) {
        throw new IllegalArgumentException("Product not found: " + ing.getProductId());
      }
      return RecipeIngredient.builder()
          .productId(ing.getProductId())
          .quantity(ing.getQuantity())
          .unit(ing.getUnit())
          .build();
    }).collect(Collectors.toList()));

    recipe.setSteps(dto.getSteps().stream().map(s -> RecipeStep.builder()
        .stepNumber(s.getStepNumber())
        .title(s.getTitle())
        .description(s.getDescription())
        .imageUrl(s.getImageUrl())
        .timerMinutes(s.getTimerMinutes())
        .build()).collect(Collectors.toList()));

    return inflateResponse(recipePersistencePort.save(recipe));
  }

  @Override
  public void deleteRecipe(Long id, String username) {
    Recipe recipe = recipePersistencePort.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + id));

    User user = userPersistencePort.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!recipe.getAuthorId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
      throw new AccessDeniedException("Only the author can delete this recipe");
    }

    recipePersistencePort.deleteById(id);
  }

  @Override
  public RecipeResponseDTO rateRecipe(Long recipeId, Integer rating, String comment, String username) {
    if (rating < 1 || rating > 5) {
      throw new IllegalArgumentException("Rating must be between 1 and 5");
    }

    User user = userPersistencePort.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (ratingPersistencePort.existsByRecipeIdAndUserId(recipeId, user.getId())) {
      throw new DataIntegrityViolationException("Ya has valorado esta receta");
    }

    Recipe recipe = recipePersistencePort.findById(recipeId)
        .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));

    RecipeRating newRating = RecipeRating.builder()
        .recipeId(recipeId)
        .userId(user.getId())
        .rating(rating)
        .comment(comment)
        .build();

    ratingPersistencePort.save(newRating);
    return inflateResponse(recipe);
  }

  // devuelve recetas ordenadas por % de ingredientes que ya tenemos
  @Override
  public List<RecipeSuggestionDTO> getSuggestionsForHousehold(Long householdId, String username) {
    User user = userPersistencePort.findByUsername(username).orElseThrow();

    if (!householdMembersPersistencePort.existsByUserIdAndHouseholdId(user.getId(), householdId)) {
      throw new AccessDeniedException("You are not a member of household " + householdId);
    }

    List<PantryItem> pantry = pantryItemPersistencePort.findByHouseholdId(householdId);

    Set<Long> availableProductIds = pantry.stream()
        .filter(p -> p.getQuantity() != null && p.getQuantity().doubleValue() > 0)
        .map(PantryItem::getProductId)
        .collect(Collectors.toSet());

    List<Recipe> allRecipes = recipePersistencePort.findAll();
    List<RecipeSuggestionDTO> suggestions = new ArrayList<>();

    for (Recipe recipe : allRecipes) {
      int totalNeeded = recipe.getIngredients().size();
      if (totalNeeded == 0)
        continue; // Skip empty recipes

      int matchedCount = 0;
      List<RecipeSuggestionDTO.MissingIngredientDTO> missing = new ArrayList<>();

      for (RecipeIngredient reqIng : recipe.getIngredients()) {
        if (availableProductIds.contains(reqIng.getProductId())) {
          matchedCount++;
        } else {
          Product p = productPersistencePort.findById(reqIng.getProductId()).orElse(null);
          if (p != null) {
            missing.add(RecipeSuggestionDTO.MissingIngredientDTO.builder()
                .productId(p.getId())
                .productName(p.getName())
                .quantityNeeded(reqIng.getQuantity())
                .unit(reqIng.getUnit())
                .build());
          }
        }
      }

      double matchRatio = (double) matchedCount / totalNeeded;

      // descartamos recetas donde no tenemos ni un ingrediente
      if (matchRatio > 0.0) {
        List<RecipeRating> recipeRatings = ratingPersistencePort.findByRecipeId(recipe.getId());
        double recipeAvg = recipeRatings.isEmpty() ? 0.0
            : Math.round(recipeRatings.stream().mapToInt(RecipeRating::getRating).average().orElse(0.0) * 10.0) / 10.0;
        suggestions.add(RecipeSuggestionDTO.builder()
            .id(recipe.getId())
            .title(recipe.getTitle())
            .description(recipe.getDescription())
            .imageUrl(recipe.getImageUrl())
            .averageRating(recipeAvg)
            .ratingsCount(recipeRatings.size())
            .matchPercentage(Math.round(matchRatio * 100.0 * 10.0) / 10.0)
            .missingIngredients(missing)
            .build());
      }
    }

    suggestions.sort(Comparator.comparing(RecipeSuggestionDTO::getMatchPercentage).reversed());

    return suggestions;
  }

  @Override
  public int addMissingToShoppingList(Long householdId, Long recipeId, String username) {
    User user = userPersistencePort.findByUsername(username).orElseThrow();

    if (!householdMembersPersistencePort.existsByUserIdAndHouseholdId(user.getId(), householdId)) {
      throw new AccessDeniedException("You are not a member of household " + householdId);
    }

    Recipe recipe = recipePersistencePort.findById(recipeId)
        .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + recipeId));

    Set<Long> availableProductIds = pantryItemPersistencePort.findByHouseholdId(householdId).stream()
        .filter(p -> p.getQuantity() != null && p.getQuantity().compareTo(java.math.BigDecimal.ZERO) > 0)
        .map(PantryItem::getProductId)
        .collect(Collectors.toSet());

    Set<Long> alreadyInCart = shoppingItemPersistencePort.findByHouseholdId(householdId).stream()
        .filter(i -> !i.isChecked())
        .map(ShoppingItem::getProductId)
        .collect(Collectors.toSet());

    int count = 0;
    for (RecipeIngredient ing : recipe.getIngredients()) {
      if (!availableProductIds.contains(ing.getProductId()) && !alreadyInCart.contains(ing.getProductId())) {
        shoppingItemPersistencePort.save(ShoppingItem.builder()
            .householdId(householdId)
            .productId(ing.getProductId())
            .quantity(ing.getQuantity())
            .checked(false)
            .addedAt(LocalDateTime.now())
            .build());
        count++;
      }
    }
    return count;
  }

  private RecipeResponseDTO inflateResponse(Recipe recipe) {
    String authorName = userPersistencePort.findById(recipe.getAuthorId())
        .map(User::getUsername).orElse("Unknown");

    List<RecipeResponseDTO.IngredientResponseDTO> inflatedIngredients = recipe.getIngredients().stream()
        .map(ing -> {
          Product p = productPersistencePort.findById(ing.getProductId()).orElse(null);
          return RecipeResponseDTO.IngredientResponseDTO.builder()
              .id(ing.getId())
              .productId(ing.getProductId())
              .productName(p != null ? p.getName() : "Unknown Product")
              .productImageUrl(p != null ? p.getImageUrl() : null)
              .quantity(ing.getQuantity())
              .unit(ing.getUnit())
              .build();
        }).collect(Collectors.toList());

    List<RecipeResponseDTO.StepResponseDTO> inflatedSteps = recipe.getSteps().stream()
        .map(step -> RecipeResponseDTO.StepResponseDTO.builder()
            .id(step.getId())
            .stepNumber(step.getStepNumber())
            .title(step.getTitle())
            .description(step.getDescription())
            .imageUrl(step.getImageUrl())
            .timerMinutes(step.getTimerMinutes())
            .build())
        .collect(Collectors.toList());

    List<RecipeRating> ratings = ratingPersistencePort.findByRecipeId(recipe.getId());
    double avg = ratings.isEmpty() ? 0.0
        : Math.round(ratings.stream().mapToInt(RecipeRating::getRating).average().orElse(0.0) * 10.0) / 10.0;

    return RecipeResponseDTO.builder()
        .id(recipe.getId())
        .title(recipe.getTitle())
        .description(recipe.getDescription())
        .imageUrl(recipe.getImageUrl())
        .authorId(recipe.getAuthorId())
        .authorUsername(authorName)
        .averageRating(avg)
        .ratingsCount(ratings.size())
        .createdAt(recipe.getCreatedAt())
        .cookingTimeMinutes(recipe.getCookingTimeMinutes())
        .difficulty(recipe.getDifficulty())
        .servings(recipe.getServings())
        .ingredients(inflatedIngredients)
        .steps(inflatedSteps)
        .build();
  }

}

