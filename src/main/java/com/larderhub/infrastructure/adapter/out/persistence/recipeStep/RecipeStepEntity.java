package com.larderhub.infrastructure.adapter.out.persistence.recipeStep;

import com.larderhub.infrastructure.adapter.out.persistence.recipe.RecipeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recipe_steps")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecipeStepEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipe_id", nullable = false)
  private RecipeEntity recipe;

  @Column(name = "step_order", nullable = false)
  private Integer stepOrder;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String instruction;
}
