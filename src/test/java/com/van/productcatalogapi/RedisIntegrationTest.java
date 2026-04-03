package com.van.productcatalogapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RedisAutoConfiguration.class)
@Testcontainers
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
class RedisIntegrationTest {

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();

    }

    @Test
    void deveSalvarERecuperarValor() {
        redisTemplate.opsForValue().set("produto:1", "Notebook Pro");

        String valor = redisTemplate.opsForValue().get("produto:1");

        assertThat(valor).isEqualTo("Notebook Pro");
    }

    @Test
    void deveIncrementarContador() {
        redisTemplate.opsForValue().set("contador:ip:127.0.0.1", "0");
        redisTemplate.opsForValue().increment("contador:ip:127.0.0.1");
        redisTemplate.opsForValue().increment("contador:ip:127.0.0.1");
        redisTemplate.opsForValue().increment("contador:ip:127.0.0.1");

        String valor = redisTemplate.opsForValue().get("contador:ip:127.0.0.1");

        assertThat(valor).isEqualTo("3");
    }

    @Test
    void deveDecrementarContador() {
        redisTemplate.opsForValue().set("estoque:produto:1", "10");

        redisTemplate.opsForValue().decrement("estoque:produto:1");
        redisTemplate.opsForValue().decrement("estoque:produto:1");

        String valor = redisTemplate.opsForValue().get("estoque:produto:1");

        assertThat(valor).isEqualTo("8");
    }

    @Test
    void deveExpirarChaveAposTempoDefinido() throws InterruptedException {
        redisTemplate.opsForValue().set("sessao:usuario:1", "dados-da-sessao");
        redisTemplate.expire("sessao:usuario:1", java.time.Duration.ofSeconds(1));

        Thread.sleep(1500);

        String valor = redisTemplate.opsForValue().get("sessao:usuario:1");

        assertThat(valor).isNull();
    }

    @Test
    void deveRegistrarERecuperarRankingDeProdutos() {
        redisTemplate.opsForZSet().incrementScore("trending", "produto:1", 5);
        redisTemplate.opsForZSet().incrementScore("trending", "produto:2", 10);
        redisTemplate.opsForZSet().incrementScore("trending", "produto:3", 3);

        var top2 = redisTemplate.opsForZSet().reverseRange("trending", 0, 1);

        assertThat(top2).containsExactly("produto:2", "produto:1");
    }
}