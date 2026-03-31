package com.van.productcatalogapi.service;

import com.van.productcatalogapi.entity.Product;
import com.van.productcatalogapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repository;

    // PONTO 1: @Cacheable
    // Quando alguém buscar produto por ID, o resultado vai pro Redis.
    // Na segunda chamada, Spring intercepta e retorna do Redis direto
    // sem nem entrar no método. O banco não é tocado.
    // "products" é o nome do "balde" no Redis onde esse dado fica.
    public Product findById(Long id) {
        log.info("[BANCO] Buscando produto id={} no PostgreSQL...", id);
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<Product> findAll() {
        return repository.findAll();
    }

    public Product save(Product product) {
        return repository.save(product);
    }

    //  PONTO 2: @CacheEvict
    // Quando atualizar um produto, o cache desse ID precisa ser apagado.
    // Se não apagar, alguém vai buscar o produto atualizado e receber
    // a versão antiga do Redis. Esse é o problema de consistência.
    public Product update(Long id, Product product) {
        Product existing = findById(id);
        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        existing.setDescription(product.getDescription());
        return repository.save(existing);
    }

    //  PONTO 3: @CacheEvict no delete também
    // Mesmo motivo: apagar o produto do banco sem apagar do Redis
    // faz a API continuar servindo um produto que não existe mais.
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
