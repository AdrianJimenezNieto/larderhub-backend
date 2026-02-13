package com.larderhub.infrastructure.adapter.out.persistence.recipeIngredient;

import com.larderhub.infrastructure.adapter.out.persistence.recipe.RecipeEntity;
import com.larderhub.infrastructure.adapter.out.persistence.product.ProductEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "recipe_ingredients")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecipeIngredientEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipe_id", nullable = false)
  private RecipeEntity recipe;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private ProductEntity product;

  @Column(name = "quantity_required", nullable = false)
  private BigDecimal quantityRequired;
}
