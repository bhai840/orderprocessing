package com.ewolff.microservice.catalog.cdc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CatalogClient {

	public static class ItemPagedResources extends PagedResources<Item> {
	}

	private final RestTemplate restTemplate;
	private final String catalogServiceHost;
	private final long catalogServicePort;

	@Autowired
	public CatalogClient(@Value("${catalog.service.host:catalog}") String catalogServiceHost,
			@Value("${catalog.service.port:8090}") long catalogServicePort) {
		super();
		this.restTemplate = getRestTemplate();
		this.catalogServiceHost = catalogServiceHost;
		this.catalogServicePort = catalogServicePort;
	}

	protected RestTemplate getRestTemplate() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.registerModule(new Jackson2HalModule());

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON));
		converter.setObjectMapper(mapper);

		return new RestTemplate(Collections.<HttpMessageConverter<?>>singletonList(converter));
	}

	public double price(long itemId) {
		return getOne(itemId).getPrice();
	}

	public Collection<Item> findAll() {
		PagedResources<Item> pagedResources = restTemplate.getForObject(catalogURL(), ItemPagedResources.class);
		return pagedResources.getContent();
	}

	private String catalogURL() {
		return "http://" + catalogServiceHost + ":" + catalogServicePort + "/catalog/";
	}

	public Item getOne(long itemId) {
		return restTemplate.getForObject(catalogURL() + itemId, Item.class);
	}

}
