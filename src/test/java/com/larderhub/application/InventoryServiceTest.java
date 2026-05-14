package com.larderhub.application;

import com.larderhub.domain.model.PantryItem;
import com.larderhub.domain.model.Product;
import com.larderhub.domain.model.User;
import com.larderhub.domain.ports.out.householdMembers.HouseholdMembersPersistencePort;
import com.larderhub.domain.ports.out.pantry.PantryItemPersistencePort;
import com.larderhub.domain.ports.out.product.ProductPersistencePort;
import com.larderhub.domain.ports.out.user.UserPersistencePort;
import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.PantryItemCreateDTO;
import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.PantryItemResponseDTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock private PantryItemPersistencePort pantryItemPersistencePort;
    @Mock private ProductPersistencePort productPersistencePort;
    @Mock private UserPersistencePort userPersistencePort;
    @Mock private HouseholdMembersPersistencePort householdMembersPersistencePort;
    @Mock private PushSenderService pushSenderService;

    @InjectMocks private InventoryService inventoryService;

    // --- addItem: access control ---

    @Test
    void addItem_whenUserNotMemberOfHousehold_throwsAccessDeniedException() {
        User user = User.builder().id(5L).username("ana").build();
        when(userPersistencePort.findByUsername("ana")).thenReturn(Optional.of(user));
        when(householdMembersPersistencePort.existsByUserIdAndHouseholdId(5L, 1L)).thenReturn(false);

        PantryItemCreateDTO dto = new PantryItemCreateDTO();
        dto.setProductId(100L);
        dto.setQuantity(BigDecimal.ONE);

        assertThatThrownBy(() -> inventoryService.addItem(1L, dto, "ana"))
                .isInstanceOf(AccessDeniedException.class);
    }

    // --- addItem: idempotency (product already in pantry) ---

    @Test
    void addItem_whenProductAlreadyInPantry_sumsQuantity() {
        User user = User.builder().id(5L).username("ana").build();
        Product product = Product.builder().id(100L).name("Arroz").standardUnit("kg").build();

        when(userPersistencePort.findByUsername("ana")).thenReturn(Optional.of(user));
        when(householdMembersPersistencePort.existsByUserIdAndHouseholdId(5L, 1L)).thenReturn(true);
        when(productPersistencePort.findById(100L)).thenReturn(Optional.of(product));

        PantryItem existingItem = PantryItem.builder()
                .id(1L).householdId(1L).productId(100L)
                .quantity(new BigDecimal("3.0"))
                .build();
        when(pantryItemPersistencePort.findByHouseholdIdAndProductId(1L, 100L))
                .thenReturn(Optional.of(existingItem));

        ArgumentCaptor<PantryItem> captor = ArgumentCaptor.forClass(PantryItem.class);
        when(pantryItemPersistencePort.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        PantryItemCreateDTO dto = new PantryItemCreateDTO();
        dto.setProductId(100L);
        dto.setQuantity(new BigDecimal("2.0"));

        inventoryService.addItem(1L, dto, "ana");

        assertThat(captor.getValue().getQuantity()).isEqualByComparingTo(new BigDecimal("5.0"));
    }

    @Test
    void addItem_whenBothHaveExpirationDate_keepsNearerDate() {
        User user = User.builder().id(5L).username("ana").build();
        Product product = Product.builder().id(100L).name("Leche").standardUnit("l").build();

        when(userPersistencePort.findByUsername("ana")).thenReturn(Optional.of(user));
        when(householdMembersPersistencePort.existsByUserIdAndHouseholdId(5L, 1L)).thenReturn(true);
        when(productPersistencePort.findById(100L)).thenReturn(Optional.of(product));

        LocalDate nearDate = LocalDate.of(2026, 5, 1);
        LocalDate farDate = LocalDate.of(2026, 6, 1);

        PantryItem existingItem = PantryItem.builder()
                .id(1L).householdId(1L).productId(100L)
                .quantity(new BigDecimal("1.0"))
                .expirationDate(farDate)
                .build();
        when(pantryItemPersistencePort.findByHouseholdIdAndProductId(1L, 100L))
                .thenReturn(Optional.of(existingItem));

        ArgumentCaptor<PantryItem> captor = ArgumentCaptor.forClass(PantryItem.class);
        when(pantryItemPersistencePort.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        PantryItemCreateDTO dto = new PantryItemCreateDTO();
        dto.setProductId(100L);
        dto.setQuantity(new BigDecimal("1.0"));
        dto.setExpirationDate(nearDate);

        inventoryService.addItem(1L, dto, "ana");

        // The earlier (safer) date must win
        assertThat(captor.getValue().getExpirationDate()).isEqualTo(nearDate);
    }

    @Test
    void addItem_whenProductNotInPantry_createsNewItem() {
        User user = User.builder().id(5L).username("ana").build();
        Product product = Product.builder().id(100L).name("Aceite").standardUnit("l").build();

        when(userPersistencePort.findByUsername("ana")).thenReturn(Optional.of(user));
        when(householdMembersPersistencePort.existsByUserIdAndHouseholdId(5L, 1L)).thenReturn(true);
        when(productPersistencePort.findById(100L)).thenReturn(Optional.of(product));
        when(pantryItemPersistencePort.findByHouseholdIdAndProductId(1L, 100L))
                .thenReturn(Optional.empty());

        ArgumentCaptor<PantryItem> captor = ArgumentCaptor.forClass(PantryItem.class);
        PantryItem savedItem = PantryItem.builder().id(99L).householdId(1L).productId(100L)
                .quantity(new BigDecimal("0.5")).build();
        when(pantryItemPersistencePort.save(captor.capture())).thenReturn(savedItem);

        PantryItemCreateDTO dto = new PantryItemCreateDTO();
        dto.setProductId(100L);
        dto.setQuantity(new BigDecimal("0.5"));

        inventoryService.addItem(1L, dto, "ana");

        assertThat(captor.getValue().getHouseholdId()).isEqualTo(1L);
        assertThat(captor.getValue().getProductId()).isEqualTo(100L);
        assertThat(captor.getValue().getQuantity()).isEqualByComparingTo(new BigDecimal("0.5"));
    }

    // --- getExpiredItems: boundary date ---

    @Test
    void getExpiredItems_passesTodayAsDateToPort() {
        User user = User.builder().id(5L).username("ana").build();
        Product product = Product.builder().id(100L).name("Yogur").standardUnit("ud").build();

        when(userPersistencePort.findByUsername("ana")).thenReturn(Optional.of(user));
        when(householdMembersPersistencePort.existsByUserIdAndHouseholdId(5L, 1L)).thenReturn(true);

        PantryItem expiredItem = PantryItem.builder()
                .id(1L).householdId(1L).productId(100L)
                .quantity(BigDecimal.ONE)
                .expirationDate(LocalDate.now().minusDays(1))
                .build();

        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        when(pantryItemPersistencePort.findExpiredItems(eq(1L), dateCaptor.capture()))
                .thenReturn(List.of(expiredItem));
        when(productPersistencePort.findAll()).thenReturn(List.of(product));

        List<PantryItemResponseDTO> result = inventoryService.getExpiredItems(1L, "ana");

        assertThat(result).hasSize(1);
        // The service must pass today's date — not yesterday, not a hardcoded value
        assertThat(dateCaptor.getValue()).isEqualTo(LocalDate.now());
    }

    // --- listItems: tolerancia a productos huérfanos ---

    @Test
    void listItems_skipsOrphanedPantryItems() {
        User user = User.builder().id(5L).username("ana").build();
        when(userPersistencePort.findByUsername("ana")).thenReturn(Optional.of(user));
        when(householdMembersPersistencePort.existsByUserIdAndHouseholdId(5L, 1L)).thenReturn(true);

        Product valid = Product.builder().id(100L).name("Arroz").standardUnit("kg").build();
        // Solo el producto 100 está en el catálogo; el 999 está huérfano
        when(productPersistencePort.findAll()).thenReturn(List.of(valid));

        when(pantryItemPersistencePort.findByHouseholdId(1L)).thenReturn(List.of(
                PantryItem.builder().id(1L).householdId(1L).productId(100L).quantity(BigDecimal.ONE).build(),
                PantryItem.builder().id(2L).householdId(1L).productId(999L).quantity(BigDecimal.ONE).build()
        ));

        List<PantryItemResponseDTO> result = inventoryService.listItems(1L, "ana");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(100L);
    }

    @Test
    void getExpiredItems_skipsOrphanedPantryItems() {
        User user = User.builder().id(5L).username("ana").build();
        when(userPersistencePort.findByUsername("ana")).thenReturn(Optional.of(user));
        when(householdMembersPersistencePort.existsByUserIdAndHouseholdId(5L, 1L)).thenReturn(true);

        Product valid = Product.builder().id(100L).name("Yogur").standardUnit("ud").build();
        when(productPersistencePort.findAll()).thenReturn(List.of(valid));

        when(pantryItemPersistencePort.findExpiredItems(eq(1L), any(LocalDate.class))).thenReturn(List.of(
                PantryItem.builder().id(1L).householdId(1L).productId(100L).quantity(BigDecimal.ONE)
                        .expirationDate(LocalDate.now().minusDays(1)).build(),
                PantryItem.builder().id(2L).householdId(1L).productId(999L).quantity(BigDecimal.ONE)
                        .expirationDate(LocalDate.now().minusDays(2)).build()
        ));

        List<PantryItemResponseDTO> result = inventoryService.getExpiredItems(1L, "ana");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(100L);
    }
}
