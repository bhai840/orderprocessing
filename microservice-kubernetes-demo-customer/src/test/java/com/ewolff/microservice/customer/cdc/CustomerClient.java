package com.ewolff.microservice.customer.cdc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CustomerClient {

	private RestTemplate restTemplate;
	private String customerServiceHost;
	private long customerServicePort;

	static class CustomerPagedResources extends PagedResources<Customer> {

	}

	@Autowired
	public CustomerClient(@Value("${customer.service.host:customer}") String customerServiceHost,
			@Value("${customer.service.port:8090}") long customerServicePort) {
		super();
		this.restTemplate = getRestTemplate();
		this.customerServiceHost = customerServiceHost;
		this.customerServicePort = customerServicePort;
	}

	public boolean isValidCustomerId(long customerId) {
		RestTemplate restTemplate = new RestTemplate();
		try {
			ResponseEntity<String> entity = restTemplate.getForEntity(customerURL() + customerId, String.class);
			return entity.getStatusCode().is2xxSuccessful();
		} catch (final HttpClientErrorException e) {
			if (e.getStatusCode().value() == 404)
				return false;
			else
				throw e;
		}
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

	public Collection<Customer> findAll() {
		PagedResources<Customer> pagedResources = getRestTemplate().getForObject(customerURL(),
				CustomerPagedResources.class);
		return pagedResources.getContent();
	}

	private String customerURL() {
		return "http://" + customerServiceHost + ":" + customerServicePort + "/customer/";
	}

	public Customer getOne(long customerId) {
		return restTemplate.getForObject(customerURL() + customerId, Customer.class);
	}
}
