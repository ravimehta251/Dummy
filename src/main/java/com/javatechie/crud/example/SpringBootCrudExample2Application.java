package com.javatechie.crud.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import com.javatechie.crud.example.entity.Product;
import com.javatechie.crud.example.repository.ProductRepository;

@SpringBootApplication
public class SpringBootCrudExample2Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootCrudExample2Application.class, args);
	}

	@Bean
	CommandLineRunner seedProducts(ProductRepository repository) {
		return args -> {
			if (repository.count() == 0) {
				repository.saveAll(java.util.List.of(
						new Product(0, "Laptop", 15, 999.99),
						new Product(0, "Laptop Stand", 30, 49.99),
						new Product(0, "Keyboard", 40, 79.99),
						new Product(0, "Wireless Mouse", 50, 29.99)));
			}
		};
	}

}
