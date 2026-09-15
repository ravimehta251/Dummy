package com.javatechie.crud.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SpringBootCrudExample2ApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void v1HealthReturnsVersionInformation() throws Exception {
		mockMvc.perform(get("/v1/health").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("UP")))
				.andExpect(jsonPath("$.version", is("v1")));
	}

	@Test
	void v1ProductsReturnsSeededProducts() throws Exception {
		mockMvc.perform(get("/v1/products").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(4)));
	}

	@Test
	void v11SearchFindsProductsByKeyword() throws Exception {
		mockMvc.perform(get("/v1.1/products/search").param("keyword", "laptop"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].name", is("Laptop")));
	}

	@Test
	void v2SearchSupportsPagination() throws Exception {
		mockMvc.perform(get("/v2/products/search")
					.param("keyword", "laptop")
					.param("page", "0")
					.param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.totalElements", is(2)));
	}

	@Test
	void v2SearchRejectsInvalidParameters() throws Exception {
		mockMvc.perform(get("/v2/products/search").param("keyword", " "))
				.andExpect(status().isBadRequest());

		mockMvc.perform(get("/v2/products/search")
					.param("keyword", "laptop")
					.param("page", "-1"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(get("/v2/products/search")
					.param("keyword", "laptop")
					.param("size", "101"))
				.andExpect(status().isBadRequest());
	}

}
