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
@Table(name = "recipe_steps")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeStepEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipe_id", nullable = false)
  @ToString.Exclude
  private RecipeEntity recipe;

  @Column(name = "step_number", nullable = false)
  private Integer stepNumber;

  @Column(name = "step_title", length = 100)
  private String stepTitle;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String description;

  @Column(name = "image_url", columnDefinition = "TEXT")
  private String imageUrl;

  @Column(name = "timer_minutes")
  private Integer timerMinutes;
}
