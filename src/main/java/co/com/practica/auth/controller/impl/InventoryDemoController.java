package co.com.practica.auth.controller.impl;

import co.com.practica.auth.dto.ApiResponse;
import co.com.practica.auth.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Portfolio demo for module-scoped AuthZ (INVENTARIO).
 * <ul>
 *   <li>{@code PERM_INVENTARIO_PRECIO_READ} — see prices</li>
 *   <li>{@code PERM_INVENTARIO_PRECIO_WRITE} — change prices</li>
 *   <li>{@code PERM_INVENTARIO_STOCK_WRITE} — change quantities</li>
 * </ul>
 * Seller (VENDEDOR) seed has READ only — write endpoints return 403.
 */
@Log4j2
@Validated
@RestController
@RequestMapping("/api/demo/inventario")
public class InventoryDemoController {

    private final Map<String, Product> catalog = new ConcurrentHashMap<>();

    public InventoryDemoController() {
        catalog.put("SKU-001", new Product("SKU-001", "Café 500g", new BigDecimal("18500"), 40));
        catalog.put("SKU-002", new Product("SKU-002", "Azúcar 1kg", new BigDecimal("6200"), 120));
        catalog.put("SKU-003", new Product("SKU-003", "Leche 1L", new BigDecimal("4500"), 80));
    }

    @GetMapping("/productos")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_INVENTARIO_PRECIO_READ')")
    public ResponseEntity<ApiResponse> listProducts() {
        List<Product> items = new ArrayList<>(catalog.values());
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    @PutMapping("/productos/precio")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_INVENTARIO_PRECIO_WRITE')")
    public ResponseEntity<ApiResponse> updatePrice(@Valid @RequestBody PriceUpdate body) {
        Product p = requireProduct(body.getSku());
        p.setPrice(body.getPrice());
        log.info("Inventory price updated: {} -> {}", body.getSku(), body.getPrice());
        return ResponseEntity.ok(ApiResponse.ok(p));
    }

    @PutMapping("/productos/stock")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_INVENTARIO_STOCK_WRITE')")
    public ResponseEntity<ApiResponse> updateStock(@Valid @RequestBody StockUpdate body) {
        Product p = requireProduct(body.getSku());
        p.setQuantity(body.getQuantity());
        log.info("Inventory stock updated: {} -> {}", body.getSku(), body.getQuantity());
        return ResponseEntity.ok(ApiResponse.ok(p));
    }

    private Product requireProduct(String sku) {
        Product p = catalog.get(sku);
        if (p == null) {
            throw new ResourceNotFoundException("SKU not found: " + sku);
        }
        return p;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {
        private String sku;
        private String name;
        private BigDecimal price;
        private int quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceUpdate {
        @NotBlank
        private String sku;
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        private BigDecimal price;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockUpdate {
        @NotBlank
        private String sku;
        @Min(0)
        private int quantity;
    }
}
