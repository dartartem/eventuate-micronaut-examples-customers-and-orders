package net.chrisrichardson.eventstore.examples.customersandorders.ordersservice.backend;

import io.micronaut.context.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CustomerServiceProxy implements CustomerService {

  @Value("${customer.service.url}")
  private String customerServiceUrl;

  private RestTemplate restTemplate;

  @Inject
  public CustomerServiceProxy(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public void setCustomerServiceUrl(String customerServiceUrl) {
    this.customerServiceUrl = customerServiceUrl;
  }

  @Override
  public void verifyCustomerCustomerId(String customerId) {
    Assert.notNull(customerServiceUrl, "Should not be null: " + customerServiceUrl);
    ResponseEntity<Customer> result = null;
    try {
      result = restTemplate.getForEntity(customerServiceUrl, Customer.class, customerId);
    } catch (HttpClientErrorException e) {
      switch (e.getStatusCode()) {
        case NOT_FOUND:
          throw new CustomerNotFoundException();
        default:
          unrecognizedStatusCode(customerId, e.getStatusCode());
      }
    }
    switch (result.getStatusCode()) {
      case OK:
        return;
      case NOT_FOUND:
        throw new CustomerNotFoundException();
      default:
        unrecognizedStatusCode(customerId, result.getStatusCode());
    }
  }

  private void unrecognizedStatusCode(String customerId, HttpStatus statusCode) {
    throw new RuntimeException(String.format("Unrecognized status code %s when fetching customerId %s",
            statusCode, customerId));
  }

}
