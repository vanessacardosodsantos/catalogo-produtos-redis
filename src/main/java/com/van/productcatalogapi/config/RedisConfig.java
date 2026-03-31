package com.van.productcatalogapi.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    // TODO NÍVEL 2: configurar aqui:
    // 1. RedisTemplate<String, Object> com serialização JSON (Jackson)
    //    — sem isso o Redis guarda bytes ilegíveis em vez de JSON
    // 2. RedisCacheManager com TTL padrão de 10 minutos
    //    — o @Cacheable vai usar esse manager automaticamente
    // 3. CacheManagerCustomizer para configurar caches individuais
    //    — ex: cache de produtos com TTL diferente do cache de trending
}
