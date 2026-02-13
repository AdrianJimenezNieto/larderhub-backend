package com.larderhub.infrastructure.adapter.out.persistence.pantry;

import com.larderhub.infrastructure.adapter.out.persistence.household.HouseholdEntity;
import com.larderhub.infrastructure.adapter.out.persistence.product.ProductEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pantry_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PantryItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "household_id", nullable = false)
  private HouseholdEntity household;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private ProductEntity product;

  @Column(nullable = false)
  private BigDecimal quantity;

  @Column(name = "expiration_date")
  private LocalDate expirationDate;
}
