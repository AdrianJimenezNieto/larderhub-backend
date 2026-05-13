package com.larderhub.application;

import com.larderhub.domain.model.*;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock private RecipePersistencePort recipePersistencePort;
    @Mock private RecipeRatingPersistencePort ratingPersistencePort;
    @Mock private ProductPersistencePort productPersistencePort;
    @Mock private UserPersistencePort userPersistencePort;
    @Mock private PantryItemPersistencePort pantryItemPersistencePort;
    @Mock private HouseholdMembersPersistencePort householdMembersPersistencePort;
    @Mock private ShoppingItemPersistencePort shoppingItemPersistencePort;

    @InjectMocks private RecipeService recipeService;

    // --- createRecipe ---

    @Test
    void createRecipe_whenUserNotFound_throwsIllegalArgumentException() {
        when(userPersistencePort.findByUsername("ghost")).thenReturn(Optional.empty());

        RecipeCreateDTO dto = buildMinimalRecipeDTO();

        assertThatThrownBy(() -> recipeService.createRecipe(dto, "ghost"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createRecipe_happyPath_savesAndReturnsDTO() {
        User author = User.builder().id(1L).username("chef").email("chef@test.com").build();
        when(userPersistencePort.findByUsername("chef")).thenReturn(Optional.of(author));
        when(productPersistencePort.findById(100L)).thenReturn(
                Optional.of(Product.builder().id(100L).name("Tomate").standardUnit("kg").build()));

        Recipe savedRecipe = Recipe.builder()
                .id(10L)
                .title("Gazpacho")
                .authorId(1L)
                .ingredients(Collections.emptyList())
                .steps(Collections.emptyList())
                .build();
        when(recipePersistencePort.save(any(Recipe.class))).thenReturn(savedRecipe);
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(author));
        when(ratingPersistencePort.findByRecipeId(10L)).thenReturn(Collections.emptyList());

        RecipeCreateDTO dto = buildMinimalRecipeDTO();
        RecipeResponseDTO result = recipeService.createRecipe(dto, "chef");

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("Gazpacho");
        verify(recipePersistencePort).save(any(Recipe.class));
    }

    // --- deleteRecipe ---

    @Test
    void deleteRecipe_whenRecipeNotFound_throwsIllegalArgumentException() {
        when(recipePersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.deleteRecipe(99L, "any"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Recipe not found");
    }

    @Test
    void deleteRecipe_whenCallerIsNotAuthor_throwsAccessDeniedException() {
        Recipe recipe = Recipe.builder().id(5L).authorId(99L)
                .ingredients(Collections.emptyList()).steps(Collections.emptyList()).build();
        User otherUser = User.builder().id(10L).username("other").role("USER").build();

        when(recipePersistencePort.findById(5L)).thenReturn(Optional.of(recipe));
        when(userPersistencePort.findByUsername("other")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> recipeService.deleteRecipe(5L, "other"))
                .isInstanceOf(AccessDeniedException.class);

        verify(recipePersistencePort, never()).deleteById(any());
    }

    @Test
    void deleteRecipe_whenCallerIsAuthor_deletesSuccessfully() {
        Recipe recipe = Recipe.builder().id(5L).authorId(10L)
                .ingredients(Collections.emptyList()).steps(Collections.emptyList()).build();
        User author = User.builder().id(10L).username("chef").role("USER").build();

        when(recipePersistencePort.findById(5L)).thenReturn(Optional.of(recipe));
        when(userPersistencePort.findByUsername("chef")).thenReturn(Optional.of(author));

        recipeService.deleteRecipe(5L, "chef");

        verify(recipePersistencePort).deleteById(5L);
    }

    @Test
    void deleteRecipe_whenCallerIsAdmin_deletesAnyRecipe() {
        Recipe recipe = Recipe.builder().id(5L).authorId(99L)
                .ingredients(Collections.emptyList()).steps(Collections.emptyList()).build();
        User admin = User.builder().id(1L).username("admin").role("ADMIN").build();

        when(recipePersistencePort.findById(5L)).thenReturn(Optional.of(recipe));
        when(userPersistencePort.findByUsername("admin")).thenReturn(Optional.of(admin));

        recipeService.deleteRecipe(5L, "admin");

        verify(recipePersistencePort).deleteById(5L);
    }

    // --- getSuggestionsForHousehold ---

    @Test
    void getSuggestions_withFullMatch_returns100Percent() {
        User user = User.builder().id(10L).username("chef").build();
        when(userPersistencePort.findByUsername("chef")).thenReturn(Optional.of(user));
        when(householdMembersPersistencePort.existsByUserIdAndHouseholdId(10L, 1L)).thenReturn(true);

        Product prod100 = Product.builder().id(100L).name("Patata").build();
        Product prod200 = Product.builder().id(200L).name("Huevo").build();
        when(productPersistencePort.findAll()).thenReturn(List.of(prod100, prod200));

        when(pantryItemPersistencePort.findByHouseholdId(1L)).thenReturn(List.of(
                PantryItem.builder().productId(100L).quantity(BigDecimal.valueOf(2)).build(),
                PantryItem.builder().productId(200L).quantity(BigDecimal.valueOf(1)).build()
        ));

        Recipe recipe = Recipe.builder().id(5L).title("Tortilla").authorId(1L)
                .ingredients(List.of(
                        RecipeIngredient.builder().productId(100L).quantity(1.0).build(),
                        RecipeIngredient.builder().productId(200L).quantity(2.0).build()
                ))
                .steps(Collections.emptyList())
                .build();
        when(recipePersistencePort.findAll()).thenReturn(List.of(recipe));
        when(ratingPersistencePort.findByRecipeId(5L)).thenReturn(Collections.emptyList());

        List<RecipeSuggestionDTO> result = recipeService.getSuggestionsForHousehold(1L, "chef");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMatchPercentage()).isEqualTo(100.0);
        assertThat(result.get(0).getMissingIngredients()).isEmpty();
    }

    @Test
    void getSuggestions_withPartialMatch_returnsCorrectPercentageAndMissingIngredients() {
        User user = User.builder().id(10L).username("chef").build();
        when(userPersistencePort.findByUsername("chef")).thenReturn(Optional.of(user));
        when(householdMembersPersistencePort.existsByUserIdAndHouseholdId(10L, 1L)).thenReturn(true);

        Product prod100 = Product.builder().id(100L).name("Patata").build();
        Product prod200 = Product.builder().id(200L).name("Aceite").build();
        when(productPersistencePort.findAll()).thenReturn(List.of(prod100, prod200));

        // Solo el producto 100 está en el pantry — el 200 falta
        when(pantryItemPersistencePort.findByHouseholdId(1L)).thenReturn(List.of(
                PantryItem.builder().productId(100L).quantity(BigDecimal.valueOf(2)).build()
        ));

        Recipe recipe = Recipe.builder().id(5L).title("Tortilla").authorId(1L)
                .ingredients(List.of(
                        RecipeIngredient.builder().productId(100L).quantity(1.0).unit("ud").build(),
                        RecipeIngredient.builder().productId(200L).quantity(0.5).unit("l").build()
                ))
                .steps(Collections.emptyList())
                .build();
        when(recipePersistencePort.findAll()).thenReturn(List.of(recipe));
        when(ratingPersistencePort.findByRecipeId(5L)).thenReturn(Collections.emptyList());

        List<RecipeSuggestionDTO> result = recipeService.getSuggestionsForHousehold(1L, "chef");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMatchPercentage()).isEqualTo(50.0);
        assertThat(result.get(0).getMissingIngredients()).hasSize(1);
        assertThat(result.get(0).getMissingIngredients().get(0).getProductName()).isEqualTo("Aceite");
    }

    @Test
    void getSuggestions_withNoIngredients_recipeIsSkipped() {
        User user = User.builder().id(10L).username("chef").build();
        when(userPersistencePort.findByUsername("chef")).thenReturn(Optional.of(user));
        when(householdMembersPersistencePort.existsByUserIdAndHouseholdId(10L, 1L)).thenReturn(true);
        when(productPersistencePort.findAll()).thenReturn(Collections.emptyList());
        when(pantryItemPersistencePort.findByHouseholdId(1L)).thenReturn(Collections.emptyList());

        Recipe emptyRecipe = Recipe.builder().id(7L).title("Sin ingredientes").authorId(1L)
                .ingredients(Collections.emptyList())
                .steps(Collections.emptyList())
                .build();
        when(recipePersistencePort.findAll()).thenReturn(List.of(emptyRecipe));

        List<RecipeSuggestionDTO> result = recipeService.getSuggestionsForHousehold(1L, "chef");

        assertThat(result).isEmpty();
    }

    @Test
    void getSuggestions_withOrphanedIngredient_doesNotInflateMatchPercentage() {
        // Receta con 4 ingredientes: 1 y 2 en pantry, 3 faltante real, 999 huérfano
        // (eliminado del catálogo pero presente en pantry y en la receta)
        // Catálogo: solo productos 1, 2, 3 (no 999)
        // Esperado: matchPercentage = 50.0 (2/4), missing.size() == 1 (solo producto 3)
        User user = User.builder().id(10L).username("chef").build();
        when(userPersistencePort.findByUsername("chef")).thenReturn(Optional.of(user));
        when(householdMembersPersistencePort.existsByUserIdAndHouseholdId(10L, 1L)).thenReturn(true);

        Product prod1 = Product.builder().id(1L).name("Tomate").build();
        Product prod2 = Product.builder().id(2L).name("Cebolla").build();
        Product prod3 = Product.builder().id(3L).name("Ajo").build();
        // producto 999 no está en el catálogo — es el huérfano
        when(productPersistencePort.findAll()).thenReturn(List.of(prod1, prod2, prod3));

        when(pantryItemPersistencePort.findByHouseholdId(1L)).thenReturn(List.of(
                PantryItem.builder().productId(1L).quantity(BigDecimal.valueOf(3)).build(),
                PantryItem.builder().productId(2L).quantity(BigDecimal.valueOf(1)).build(),
                PantryItem.builder().productId(999L).quantity(BigDecimal.valueOf(1)).build()
        ));

        Recipe recipe = Recipe.builder().id(5L).title("Sofrito").authorId(1L)
                .ingredients(List.of(
                        RecipeIngredient.builder().productId(1L).quantity(2.0).unit("ud").build(),
                        RecipeIngredient.builder().productId(2L).quantity(1.0).unit("ud").build(),
                        RecipeIngredient.builder().productId(3L).quantity(3.0).unit("ud").build(),
                        RecipeIngredient.builder().productId(999L).quantity(1.0).unit("ud").build()
                ))
                .steps(Collections.emptyList())
                .build();
        when(recipePersistencePort.findAll()).thenReturn(List.of(recipe));
        when(ratingPersistencePort.findByRecipeId(5L)).thenReturn(Collections.emptyList());

        List<RecipeSuggestionDTO> result = recipeService.getSuggestionsForHousehold(1L, "chef");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMatchPercentage()).isEqualTo(50.0);
        assertThat(result.get(0).getMissingIngredients()).hasSize(1);
        assertThat(result.get(0).getMissingIngredients().get(0).getProductId()).isEqualTo(3L);
    }

    // --- helpers ---

    private RecipeCreateDTO buildMinimalRecipeDTO() {
        RecipeCreateDTO dto = new RecipeCreateDTO();
        dto.setTitle("Gazpacho");
        dto.setDescription("Sopa fría");
        dto.setDifficulty("easy");
        dto.setServings(4);
        dto.setCookingTimeMinutes(15);

        RecipeCreateDTO.RecipeIngredientDTO ing = new RecipeCreateDTO.RecipeIngredientDTO();
        ing.setProductId(100L);
        ing.setQuantity(1.0);
        ing.setUnit("kg");
        dto.setIngredients(List.of(ing));

        RecipeCreateDTO.RecipeStepDTO step = new RecipeCreateDTO.RecipeStepDTO();
        step.setStepNumber(1);
        step.setDescription("Triturar todo");
        dto.setSteps(List.of(step));

        return dto;
    }
}
