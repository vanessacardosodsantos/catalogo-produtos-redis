package com.van.productcatalogapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProductcatalogapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductcatalogapiApplication.class, args);
	}

}
