package com.larderhub.infrastructure.adapter.in.rest.recipe;

import com.larderhub.domain.ports.in.recipe.RecipeUseCase;
import com.larderhub.infrastructure.adapter.in.rest.recipe.dto.RecipeCreateDTO;
import com.larderhub.infrastructure.adapter.in.rest.recipe.dto.RecipeResponseDTO;
import com.larderhub.infrastructure.adapter.in.rest.recipe.dto.UpdateRecipeDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
public class RecipeController {

  private final RecipeUseCase recipeUseCase;

  @PostMapping
  public ResponseEntity<RecipeResponseDTO> createRecipe(
      @Valid @RequestBody RecipeCreateDTO dto,
      @AuthenticationPrincipal UserDetails userDetails) {
    RecipeResponseDTO response = recipeUseCase.createRecipe(dto, userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<RecipeResponseDTO>> getAllRecipes() {
    return ResponseEntity.ok(recipeUseCase.getAllRecipes());
  }

  @GetMapping("/{id}")
  public ResponseEntity<RecipeResponseDTO> getRecipeById(@PathVariable Long id) {
    return ResponseEntity.ok(recipeUseCase.getRecipeById(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<RecipeResponseDTO> updateRecipe(
      @PathVariable Long id,
      @Valid @RequestBody UpdateRecipeDTO dto,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(recipeUseCase.updateRecipe(id, dto, userDetails.getUsername()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteRecipe(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    recipeUseCase.deleteRecipe(id, userDetails.getUsername());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/ratings")
  public ResponseEntity<RecipeResponseDTO> rateRecipe(
      @PathVariable Long id,
      @RequestParam Integer rating,
      @RequestParam(required = false) String comment,
      @AuthenticationPrincipal UserDetails userDetails) {
    RecipeResponseDTO response = recipeUseCase.rateRecipe(id, rating, comment, userDetails.getUsername());
    return ResponseEntity.ok(response);
  }
}
