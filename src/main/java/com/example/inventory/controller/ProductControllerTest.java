//
//package com.example.inventory.controller;
//
//import com.example.inventory.exception.ResourceNotFoundException;
//import com.example.inventory.model.Product;
//import com.example.inventory.service.ProductService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.test.web.servlet.MockMvc;
//import org.testng.annotations.Test;
//
//import java.math.BigDecimal;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static reactor.core.publisher.Mono.when;
//
//@WebMvcTest(ProductController.class)
//public class ProductControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private ProductService productService;
//
//    @Test
//    void testGetProduct_Success() throws Exception {
//        // Arrange
//        Product product = new Product(1L, "Test Product", "Description", "SKU-1",
//                BigDecimal.valueOf(10.99), 100, 1L, 1L, null, null, 0);
//        when(productService.findById(1L)).thenReturn(Optional.of(product));
//
//        // Act & Assert
//        mockMvc.perform(get("/api/products/1")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(product)));
//
//        verify(productService).findById(1L);
//    }
//
//    @Test
//    void testGetProduct_NotFound() throws Exception {
//        // Arrange
//        when(productService.findById(999L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        mockMvc.perform(get("/api/products/999")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpected(status().isNotFound());
//
//        verify(productService).findById(999L);
//    }
//
//    @Test
//    void testCreateProduct_Success() throws Exception {
//        // Arrange
//        Product newProduct = new Product(null, "New Product", "Description", "SKU-NEW",
//                BigDecimal.valueOf(15.99), 50, 1L, 1L, null, null, 0);
//        Product savedProduct = new Product(1L, "New Product", "Description", "SKU-NEW",
//                BigDecimal.valueOf(15.99), 50, 1L, 1L, null, null, 0);
//
//        when(productService.save(any(Product.class))).thenReturn(savedProduct);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/products")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(newProduct)))
//                .andExpect(status().isCreated())
//                .andExpect(content().json(objectMapper.writeValueAsString(savedProduct)));
//
//        // Verify the exact product passed to service
//        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
//        verify(productService).save(productCaptor.capture());
//        Product capturedProduct = productCaptor.getValue();
//        assertEquals("New Product", capturedProduct.getName());
//        assertEquals("SKU-NEW", capturedProduct.getSku());
//    }
//
//    @Test
//    void testUpdateProduct_Success() throws Exception {
//        // Arrange
//        Product existingProduct = new Product(1L, "Existing Product", "Old Description", "SKU-1",
//                BigDecimal.valueOf(10.99), 100, 1L, 1L, null, null, 0);
//        Product updatedProduct = new Product(1L, "Updated Product", "New Description", "SKU-1",
//                BigDecimal.valueOf(19.99), 75, 1L, 1L, null, null, 0);
//
//        when(productService.findById(1L)).thenReturn(Optional.of(existingProduct));
//        when(productService.save(any(Product.class))).thenReturn(updatedProduct);
//
//        // Act & Assert
//        mockMvc.perform(put("/api/products/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updatedProduct)))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(updatedProduct)));
//
//        verify(productService).findById(1L);
//
//        // Verify the exact product passed to service
//        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
//        verify(productService).save(productCaptor.capture());
//        Product capturedProduct = productCaptor.getValue();
//        assertEquals(1L, capturedProduct.getId());
//        assertEquals("Updated Product", capturedProduct.getName());
//    }
//
//    @Test
//    void testUpdateProduct_NotFound() throws Exception {
//        // Arrange
//        Product updatedProduct = new Product(999L, "Updated Product", "New Description", "SKU-999",
//                BigDecimal.valueOf(19.99), 75, 1L, 1L, null, null, 0);
//
//        when(productService.findById(999L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        mockMvc.perform(put("/api/products/999")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updatedProduct)))
//                .andExpect(status().isNotFound());
//
//        verify(productService).findById(999L);
//        verify(productService, never()).save(any(Product.class));
//    }
//
//    @Test
//    void testDeleteProduct_Success() throws Exception {
//        // Arrange
//        Product product = new Product(1L, "Test Product", "Description", "SKU-1",
//                BigDecimal.valueOf(10.99), 100, 1L, 1L, null, null, 0);
//        when(productService.findById(1L)).thenReturn(Optional.of(product));
//
//        // Act & Assert
//        mockMvc.perform(delete("/api/products/1"))
//                .andExpect(status().isNoContent());
//
//        verify(productService).findById(1L);
//        verify(productService).delete(eq(1L), eq("SKU-1"), eq(1L), eq(1L));
//    }
//
//    @Test
//    void testDeleteProduct_NotFound() throws Exception {
//        // Arrange
//        when(productService.findById(999L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        mockMvc.perform(delete("/api/products/999"))
//                .andExpect(status().isNotFound());
//
//        verify(productService).findById(999L);
//        verify(productService, never()).delete(anyLong(), anyString(), anyLong(), anyLong());
//    }
//
//    @Test
//    void testGetAllProducts() throws Exception {
//        // Arrange
//        List<Product> products = Arrays.asList(
//                new Product(1L, "Product 1", "Description 1", "SKU-1", BigDecimal.valueOf(10.99), 100, 1L, 1L, null, null, 0),
//                new Product(2L, "Product 2", "Description 2", "SKU-2", BigDecimal.valueOf(20.99), 200, 1L, 1L, null, null, 0)
//        );
//
//        when(productService.findAll()).thenReturn(products);
//
//        // Act & Assert
//        mockMvc.perform(get("/api/products")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(products)));
//
//        verify(productService).findAll();
//    }
//
//    @Test
//    void testGetProductBySku_Success() throws Exception {
//        // Arrange
//        Product product = new Product(1L, "Test Product", "Description", "SKU-1",
//                BigDecimal.valueOf(10.99), 100, 1L, 1L, null, null, 0);
//        when(productService.findBySku("SKU-1")).thenReturn(Optional.of(product));
//
//        // Act & Assert
//        mockMvc.perform(get("/api/products/sku/SKU-1")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(product)));
//
//        verify(productService).findBySku("SKU-1");
//    }
//
//    @Test
//    void testGetProductBySku_NotFound() throws Exception {
//        // Arrange
//        when(productService.findBySku("NON-EXISTENT")).thenReturn(Optional.empty());
//
//        // Act & Assert
//        mockMvc.perform(get("/api/products/sku/NON-EXISTENT")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//
//        verify(productService).findBySku("NON-EXISTENT");
//    }
//
//    @Test
//    void testGetProductsByCategory() throws Exception {
//        // Arrange
//        List<Product> products = Arrays.asList(
//                new Product(1L, "Product 1", "Description 1", "SKU-1", BigDecimal.valueOf(10.99), 100, 1L, 1L, null, null, 0),
//                new Product(2L, "Product 2", "Description 2", "SKU-2", BigDecimal.valueOf(20.99), 200, 1L, 1L, null, null, 0)
//        );
//
//        when(productService.findByCategoryId(1L)).thenReturn(products);
//
//        // Act & Assert
//        mockMvc.perform(get("/api/products/category/1")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(products)));
//
//        verify(productService).findByCategoryId(1L);
//    }
//
//    @Test
//    void testGetProductsBySupplier() throws Exception {
//        // Arrange
//        List<Product> products = Arrays.asList(
//                new Product(1L, "Product 1", "Description 1", "SKU-1", BigDecimal.valueOf(10.99), 100, 1L, 1L, null, null, 0)
//        );
//
//        when(productService.findBySupplierId(1L)).thenReturn(products);
//
//        // Act & Assert
//        mockMvc.perform(get("/api/products/supplier/1")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(products)));
//
//        verify(productService).findBySupplierId(1L);
//    }
//
//    @Test
//    void testGetLowStockProducts() throws Exception {
//        // Arrange
//        List<Product> lowStockProducts = Arrays.asList(
//                new Product(1L, "Low Stock Product", "Description", "SKU-1", BigDecimal.valueOf(10.99), 5, 1L, 1L, null, null, 0)
//        );
//
//        when(productService.findLowStockProducts(10)).thenReturn(lowStockProducts);
//
//        // Act & Assert
//        mockMvc.perform(get("/api/products/low-stock")
//                        .param("threshold", "10")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(lowStockProducts)));
//
//        verify(productService).findLowStockProducts(10);
//    }
//
//    @Test
//    void testGetLowStockProducts_DefaultThreshold() throws Exception {
//        // Arrange
//        List<Product> lowStockProducts = Collections.emptyList();
//
//        when(productService.findLowStockProducts(10)).thenReturn(lowStockProducts);
//
//        // Act & Assert
//        mockMvc.perform(get("/api/products/low-stock")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(lowStockProducts)));
//
//        verify(productService).findLowStockProducts(10);
//    }
//
//    @Test
//    void testSearchProducts() throws Exception {
//        // Arrange
//        List<Product> searchResults = Arrays.asList(
//                new Product(1L, "Search Product", "Description", "SKU-1", BigDecimal.valueOf(10.99), 100, 1L, 1L, null, null, 0)
//        );
//
//        when(productService.searchByKeyword("search")).thenReturn(searchResults);
//
//        // Act & Assert
//        mockMvc.perform(get("/api/products/search")
//                        .param("keyword", "search")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(searchResults)));
//
//        verify(productService).searchByKeyword("search");
//    }
//
//    @Test
//    void testGetProductsByCategoryName() throws Exception {
//        // Arrange
//        List<Product> products = Arrays.asList(
//                new Product(1L, "Category Product", "Description", "SKU-1", BigDecimal.valueOf(10.99), 100, 1L, 1L, null, null, 0)
//        );
//
//        when(productService.findByCategory("Electronics")).thenReturn(products);
//
//        // Act & Assert
//        mockMvc.perform(get("/api/products/by-category-name/Electronics")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(products)));
//
//        verify(productService).findByCategory("Electronics");
//    }
//
//    @Test
//    void testUpdateProductStock_Success() throws Exception {
//        // Arrange
//        Product updatedProduct = new Product(1L, "Test Product", "Description", "SKU-1",
//                BigDecimal.valueOf(10.99), 150, 1L, 1L, null, null, 0);
//
//        when(productService.updateStock(eq(1L), eq(150))).thenReturn(updatedProduct);
//
//        // Act & Assert
//        mockMvc.perform(patch("/api/products/1/stock")
//                        .param("quantity", "150")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(updatedProduct)));
//
//        verify(productService).updateStock(1L, 150);
//    }
//
//    @Test
//    void testUpdateProductStock_NotFound() throws Exception {
//        // Arrange
//        when(productService.updateStock(eq(999L), eq(150))).thenThrow(new RuntimeException("Product not found"));
//
//        // Act & Assert
//        mockMvc.perform(patch("/api/products/999/stock")
//                        .param("quantity", "150")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//
//        verify(productService).updateStock(999L, 150);
//    }
//
//    @Test
//    void testUpdateProductStock_InvalidQuantity() throws Exception {
//        // Arrange
//        when(productService.updateStock(eq(1L), eq(-10))).thenThrow(new RuntimeException("Invalid quantity"));
//
//        // Act & Assert
//        mockMvc.perform(patch("/api/products/1/stock")
//                        .param("quantity", "-10")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//
//        verify(productService).updateStock(1L, -10);
//    }
//}