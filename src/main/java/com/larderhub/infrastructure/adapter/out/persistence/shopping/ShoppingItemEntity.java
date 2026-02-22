package com.larderhub.infrastructure.adapter.out.persistence.shopping;

import com.larderhub.infrastructure.adapter.out.persistence.household.HouseholdEntity;
import com.larderhub.infrastructure.adapter.out.persistence.product.ProductEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shopping_items")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingItemEntity {

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
  private Double quantity;

  // Marks if the item has been purchased (and moved to the pantry)
  @Column(nullable = false)
  private boolean checked;

  @Column(name = "added_at", nullable = false)
  private LocalDateTime addedAt;

  @PrePersist
  protected void onCreate() {
    if (addedAt == null) {
      addedAt = LocalDateTime.now();
    }
  }
}
