package com.van.productcatalogapi.service;

import com.van.productcatalogapi.entity.Product;
import com.van.productcatalogapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Cacheable(value = "products", key = "#id")
    public Product findById(Long id) {
        log.info("[BANCO] Buscando produto id={} no PostgreSQL...", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + id));
        log.info("[BANCO] Produto encontrado: {}. Salvando no Redis...", product.getName());
        return product;
    }

    @Cacheable(value = "products-all")
    public List<Product> findAll() {
        log.info("[BANCO] Buscando TODOS os produtos no PostgreSQL...");
        List<Product> products = productRepository.findAll();
        log.info("[BANCO] {} produtos encontrados. Salvando lista no Redis...", products.size());
        return products;
    }

    public Product save(Product product) {
        log.info("[BANCO] Salvando novo produto: {}", product.getName());
        Product saved = productRepository.save(product);
        log.info("[BANCO] Produto salvo com id={}", saved.getId());
        return saved;
    }

    @CacheEvict(value = "products", key = "#id") //USando quando o cache desse ID precisa ser apaagdo para atualizar o banco
    public Product update(Long id, Product product) {
        log.info("[CACHE] Invalidando cache do produto id={} no Redis...", id);
        Product existing = findById(id);
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setQuantity(product.getQuantity());
        Product updated = productRepository.save(existing);
        log.info("[BANCO] Produto id={} atualizado no PostgreSQL. Próxima leitura virá do banco.", id);
        return updated;
    }

    @CacheEvict(value = "products", key = "#id")
    public void delete(Long id) {
        log.info("[CACHE] Invalidando cache do produto id={} no Redis...", id);
        productRepository.deleteById(id);
        log.info("[BANCO] Produto id={} deletado do PostgreSQL.", id);
    }
}
