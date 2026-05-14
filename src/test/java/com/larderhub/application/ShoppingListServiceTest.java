package com.larderhub.application;

import com.larderhub.domain.model.PantryItem;
import com.larderhub.domain.model.Product;
import com.larderhub.domain.model.ShoppingItem;
import com.larderhub.domain.model.User;
import com.larderhub.domain.ports.out.householdMembers.HouseholdMembersPersistencePort;
import com.larderhub.domain.ports.out.pantry.PantryItemPersistencePort;
import com.larderhub.domain.ports.out.product.ProductPersistencePort;
import com.larderhub.domain.ports.out.shopping.ShoppingItemPersistencePort;
import com.larderhub.domain.ports.out.user.UserPersistencePort;
import com.larderhub.infrastructure.adapter.in.rest.shopping.dto.ShoppingItemResponseDTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock private ShoppingItemPersistencePort shoppingItemPersistencePort;
    @Mock private PantryItemPersistencePort pantryItemPersistencePort;
    @Mock private ProductPersistencePort productPersistencePort;
    @Mock private UserPersistencePort userPersistencePort;
    @Mock private HouseholdMembersPersistencePort householdMembersPersistencePort;

    @InjectMocks private ShoppingListService shoppingListService;

    // --- generateFromPantry ---

    @Test
    void generateFromPantry_whenItemBelowThreshold_addsToShoppingList() {
        setupMembership("ana", 5L, 1L);

        Product product = Product.builder().id(100L).name("Leche").standardUnit("l").build();
        when(productPersistencePort.findAll()).thenReturn(List.of(product));
        when(pantryItemPersistencePort.findByHouseholdId(1L)).thenReturn(List.of(
                PantryItem.builder().productId(100L).quantity(new BigDecimal("0.5")).build()
        ));
        when(shoppingItemPersistencePort.findByHouseholdId(1L)).thenReturn(Collections.emptyList());

        ShoppingItem saved = ShoppingItem.builder().id(9L).householdId(1L).productId(100L)
                .quantity(1.5).checked(false).build();
        when(shoppingItemPersistencePort.save(any(ShoppingItem.class))).thenReturn(saved);

        List<ShoppingItemResponseDTO> result = shoppingListService.generateFromPantry(1L, 1.0, "ana");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).isEqualTo("Leche");
        verify(shoppingItemPersistencePort).save(any(ShoppingItem.class));
    }

    @Test
    void generateFromPantry_whenItemAlreadyInCartUnchecked_skipsIt() {
        setupMembership("ana", 5L, 1L);

        Product product = Product.builder().id(100L).name("Leche").standardUnit("l").build();
        when(productPersistencePort.findAll()).thenReturn(List.of(product));
        when(pantryItemPersistencePort.findByHouseholdId(1L)).thenReturn(List.of(
                PantryItem.builder().productId(100L).quantity(new BigDecimal("0.5")).build()
        ));
        // Product 100 already in cart and unchecked
        when(shoppingItemPersistencePort.findByHouseholdId(1L)).thenReturn(List.of(
                ShoppingItem.builder().productId(100L).checked(false).build()
        ));

        List<ShoppingItemResponseDTO> result = shoppingListService.generateFromPantry(1L, 1.0, "ana");

        assertThat(result).isEmpty();
        verify(shoppingItemPersistencePort, never()).save(any());
    }

    @Test
    void generateFromPantry_whenQuantityExactlyAtThreshold_isIncluded() {
        // Boundary condition: quantity == threshold must still be included (<=)
        setupMembership("ana", 5L, 1L);

        Product product = Product.builder().id(100L).name("Arroz").standardUnit("kg").build();
        when(productPersistencePort.findAll()).thenReturn(List.of(product));
        when(pantryItemPersistencePort.findByHouseholdId(1L)).thenReturn(List.of(
                PantryItem.builder().productId(100L).quantity(new BigDecimal("1.0")).build()
        ));
        when(shoppingItemPersistencePort.findByHouseholdId(1L)).thenReturn(Collections.emptyList());

        ShoppingItem saved = ShoppingItem.builder().id(10L).householdId(1L).productId(100L)
                .quantity(1.0).checked(false).build();
        when(shoppingItemPersistencePort.save(any(ShoppingItem.class))).thenReturn(saved);

        List<ShoppingItemResponseDTO> result = shoppingListService.generateFromPantry(1L, 1.0, "ana");

        assertThat(result).hasSize(1);
    }

    // --- checkItem: moves to pantry ---

    @Test
    void checkItem_whenProductNotInPantry_createsNewPantryItem() {
        setupMembership("ana", 5L, 1L);

        when(shoppingItemPersistencePort.existsByIdAndHouseholdId(7L, 1L)).thenReturn(true);
        ShoppingItem cartItem = ShoppingItem.builder()
                .id(7L).householdId(1L).productId(100L).quantity(3.0).checked(false).build();
        when(shoppingItemPersistencePort.findById(7L)).thenReturn(Optional.of(cartItem));
        when(pantryItemPersistencePort.findByHouseholdIdAndProductId(1L, 100L))
                .thenReturn(Optional.empty());

        Product product = Product.builder().id(100L).name("Pasta").standardUnit("kg").build();
        when(productPersistencePort.findById(100L)).thenReturn(Optional.of(product));
        when(shoppingItemPersistencePort.save(any(ShoppingItem.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<PantryItem> pantryCaptor = ArgumentCaptor.forClass(PantryItem.class);
        when(pantryItemPersistencePort.save(pantryCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        shoppingListService.checkItem(1L, 7L, "ana");

        // A new pantry item must have been created with the cart quantity
        assertThat(pantryCaptor.getValue().getQuantity())
                .isEqualByComparingTo(BigDecimal.valueOf(3.0));
        assertThat(pantryCaptor.getValue().getProductId()).isEqualTo(100L);
    }

    @Test
    void checkItem_whenProductAlreadyInPantry_sumsQuantity() {
        setupMembership("ana", 5L, 1L);

        when(shoppingItemPersistencePort.existsByIdAndHouseholdId(7L, 1L)).thenReturn(true);
        ShoppingItem cartItem = ShoppingItem.builder()
                .id(7L).householdId(1L).productId(100L).quantity(3.0).checked(false).build();
        when(shoppingItemPersistencePort.findById(7L)).thenReturn(Optional.of(cartItem));

        PantryItem existing = PantryItem.builder()
                .id(1L).householdId(1L).productId(100L).quantity(new BigDecimal("2.0")).build();
        when(pantryItemPersistencePort.findByHouseholdIdAndProductId(1L, 100L))
                .thenReturn(Optional.of(existing));

        Product product = Product.builder().id(100L).name("Pasta").standardUnit("kg").build();
        when(productPersistencePort.findById(100L)).thenReturn(Optional.of(product));
        when(shoppingItemPersistencePort.save(any(ShoppingItem.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<PantryItem> pantryCaptor = ArgumentCaptor.forClass(PantryItem.class);
        when(pantryItemPersistencePort.save(pantryCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        shoppingListService.checkItem(1L, 7L, "ana");

        // 2.0 (existing) + 3.0 (cart) = 5.0
        assertThat(pantryCaptor.getValue().getQuantity())
                .isEqualByComparingTo(new BigDecimal("5.0"));
    }

    // --- listItems: tolerancia a productos huérfanos ---

    @Test
    void listItems_skipsOrphanedShoppingItems() {
        setupMembership("ana", 5L, 1L);

        Product valid = Product.builder().id(100L).name("Leche").standardUnit("l").build();
        when(productPersistencePort.findAll()).thenReturn(List.of(valid));

        when(shoppingItemPersistencePort.findByHouseholdId(1L)).thenReturn(List.of(
                ShoppingItem.builder().id(1L).householdId(1L).productId(100L).quantity(1.0).checked(false).build(),
                ShoppingItem.builder().id(2L).householdId(1L).productId(999L).quantity(1.0).checked(false).build()
        ));

        List<ShoppingItemResponseDTO> result = shoppingListService.listItems(1L, "ana");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(100L);
    }

    // --- checkItem: producto huérfano ---

    @Test
    void checkItem_whenProductIsOrphan_throwsIllegalArgumentException() {
        setupMembership("ana", 5L, 1L);

        when(shoppingItemPersistencePort.existsByIdAndHouseholdId(7L, 1L)).thenReturn(true);
        ShoppingItem cartItem = ShoppingItem.builder()
                .id(7L).householdId(1L).productId(999L).quantity(2.0).checked(false).build();
        when(shoppingItemPersistencePort.findById(7L)).thenReturn(Optional.of(cartItem));
        when(pantryItemPersistencePort.findByHouseholdIdAndProductId(1L, 999L))
                .thenReturn(Optional.empty());
        when(shoppingItemPersistencePort.save(any(ShoppingItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pantryItemPersistencePort.save(any(PantryItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productPersistencePort.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shoppingListService.checkItem(1L, 7L, "ana"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya no existe en el catálogo");
    }

    // --- helper ---

    private void setupMembership(String username, Long userId, Long householdId) {
        User user = User.builder().id(userId).username(username).build();
        when(userPersistencePort.findByUsername(username)).thenReturn(Optional.of(user));
        when(householdMembersPersistencePort.existsByUserIdAndHouseholdId(userId, householdId))
                .thenReturn(true);
    }
}
