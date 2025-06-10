package com.example.inventory.controller;

import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.model.Product;
import com.example.inventory.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@WebFluxTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductService productService;

    @Test
    void testGetProduct_Success() {
        // Arrange
        Product product = new Product(1L, "Test Product", 10.99, 100);
        when(productService.getProduct(1L)).thenReturn(Mono.just(product));

        // Act & Assert
        webTestClient.get()
                .uri("/api/products/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .isEqualTo(product);

        verify(productService).getProduct(1L);
    }

    @Test
    void testGetProduct_NotFound() {
        // Arrange
        when(productService.getProduct(999L)).thenReturn(
                Mono.error(new ResourceNotFoundException("Product with id 999 not found")));

        // Act & Assert
        webTestClient.get()
                .uri("/api/products/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product with id 999 not found")
                .jsonPath("$.status").isEqualTo(404);

        verify(productService).getProduct(999L);
    }

    @Test
    void testCreateProduct_Success() {
        // Arrange
        Product newProduct = new Product(null, "New Product", 15.99, 50);
        Product savedProduct = new Product(1L, "New Product", 15.99, 50);

        when(productService.createProduct(any(Product.class))).thenReturn(Mono.just(savedProduct));

        // Act & Assert
        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newProduct)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Product.class)
                .isEqualTo(savedProduct);

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void testUpdateProduct_Success() {
        // Arrange
        Product updatedProduct = new Product(1L, "Updated Product", 19.99, 75);

        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(Mono.just(updatedProduct));

        // Act & Assert
        webTestClient.put()
                .uri("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedProduct)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .isEqualTo(updatedProduct);

        verify(productService).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void testUpdateProduct_NotFound() {
        // Arrange
        Product updatedProduct = new Product(999L, "Updated Product", 19.99, 75);

        when(productService.updateProduct(eq(999L), any(Product.class)))
                .thenReturn(Mono.error(new ResourceNotFoundException("Product with id 999 not found")));

        // Act & Assert
        webTestClient.put()
                .uri("/api/products/999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedProduct)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product with id 999 not found")
                .jsonPath("$.status").isEqualTo(404);

        verify(productService).updateProduct(eq(999L), any(Product.class));
    }

    @Test
    void testDeleteProduct_Success() {
        // Arrange
        when(productService.deleteProduct(1L)).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.delete()
                .uri("/api/products/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(productService).deleteProduct(1L);
    }
}

