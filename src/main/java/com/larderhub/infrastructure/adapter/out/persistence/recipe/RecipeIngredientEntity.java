package com.larderhub.infrastructure.adapter.out.persistence.recipe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "recipe_ingredients")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeIngredientEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipe_id", nullable = false)
  @ToString.Exclude // Prevent circular toString issues
  private RecipeEntity recipe;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(nullable = false)
  private Double quantity;

  @Column(length = 20)
  private String unit;
}
