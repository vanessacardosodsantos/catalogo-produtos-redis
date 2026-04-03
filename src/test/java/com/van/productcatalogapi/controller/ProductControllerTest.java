package com.van.productcatalogapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.van.productcatalogapi.entity.Product;
import com.van.productcatalogapi.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;
    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Notebook Pro");
        product.setDescription("Notebook com 16GB RAM");
        product.setPrice(BigDecimal.valueOf(4999.90));
        product.setQuantity(10);
    }

    @Test
    void findById_deveRetornarProduto_quandoExistir() throws Exception {
        when(productService.findById(1L)).thenReturn(product);
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Notebook Pro"))
                .andExpect(jsonPath("$.price").value(4999.90));
    }

    @Test
    void findAll_deveRetornarListaDeProdutos() throws Exception {
        when(productService.findAll()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Notebook Pro"));
    }

    @Test
    void save_deveCriarProduto_eRetornar201() throws Exception {
        when(productService.save(any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Notebook Pro"));
    }

    @Test
    void save_deveRetornar400_quandoDadosInvalidos() throws Exception {
        Product invalido = new Product();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_deveAtualizarProduto() throws Exception {
        Product atualizado = new Product();
        atualizado.setId(1L);
        atualizado.setName("Notebook Ultra");
        atualizado.setDescription("Versão atualizada");
        atualizado.setPrice(BigDecimal.valueOf(5999.90));
        atualizado.setQuantity(5);

        when(productService.update(eq(1L), any(Product.class))).thenReturn(atualizado);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook Ultra"))
                .andExpect(jsonPath("$.price").value(5999.90));
    }

    @Test
    void delete_deveRetornar204() throws Exception {
        doNothing().when(productService).delete(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
}
