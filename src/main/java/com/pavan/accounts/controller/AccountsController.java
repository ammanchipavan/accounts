/**
 * 
 */
package com.pavan.accounts.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.pavan.accounts.config.AccountsServiceConfig;
import com.pavan.accounts.model.Accounts;
import com.pavan.accounts.model.Customer;
import com.pavan.accounts.model.CustomerDetails;
import com.pavan.accounts.model.Properties;
import com.pavan.accounts.repository.AccountsRepository;
import com.pavan.accounts.service.client.CardsFeignClient;
import com.pavan.accounts.service.client.LoansFeignClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * @author Eazy Bytes
 *
 */

@RestController
public class AccountsController {
	
	private static final Logger logger = LoggerFactory.getLogger(AccountsController.class);
	
	@Autowired
	private AccountsRepository accountsRepository;
	
	@Autowired
	AccountsServiceConfig accountsServiceConfig;
	
	@Autowired
	LoansFeignClient loansClient;
	
	@Autowired
	CardsFeignClient cardsClient;

	@PostMapping("/myAccount")
	public Accounts getAccountDetails(@RequestBody Customer customer) {

		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
		if (accounts != null) {
			return accounts;
		} else {
			return null;
		}

	}
	
	@GetMapping("/account/properties")
	public String getAccoutsProperties() throws JsonProcessingException {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		Properties properties = new Properties(accountsServiceConfig.getMsg(), accountsServiceConfig.getBuildVersion(),
				accountsServiceConfig.getMailDetails(), accountsServiceConfig.getActiveBranches());
		return ow.writeValueAsString(properties);
	}
	
	@PostMapping("/myCustomerDetails")
	// @CircuitBreaker(name = "detailsForCustomerSupportApp", fallbackMethod = "myCustomerDetailsFallback")
	@Retry(name="retryForCustomerDetails", fallbackMethod = "myCustomerDetailsFallback")
	public CustomerDetails getCustomerDetails(@RequestHeader("pavanbank-correlation-id") String correlationId, @RequestBody Customer customer) {
		logger.info("getCustomerDetails() method started");
		CustomerDetails customerDetails = new CustomerDetails();
		customerDetails.setAccounts(accountsRepository.findByCustomerId(customer.getCustomerId()));
		customerDetails.setLoans(loansClient.getLoansDetails(correlationId, customer));
		customerDetails.setCards(cardsClient.getCardDetails(correlationId, customer));
		logger.info("getCustomerDetails() method ended");
		
		return customerDetails;
	}
	
	@PostMapping("/myCustomerAccountLoanDetails")
	@CircuitBreaker(name = "detailsForCustomerSupportApp", fallbackMethod = "myCustomerAccountsLoansDetailsFallback")
	public CustomerDetails getCustomerAccountsLoansDetails(@RequestHeader("pavanbank-correlation-id") String correlationId, @RequestBody Customer customer) {
		logger.info("getCustomerAccountsLoansDetails() method started");
		CustomerDetails customerDetails = new CustomerDetails();
		customerDetails.setAccounts(accountsRepository.findByCustomerId(customer.getCustomerId()));
		customerDetails.setLoans(loansClient.getLoansDetails(correlationId, customer));
		customerDetails.setCards(cardsClient.getCardDetails(correlationId, customer));
		logger.info("getCustomerAccountsLoansDetails() method started");
		
		return customerDetails;
	}
	
	private CustomerDetails myCustomerDetailsFallback(String correlationId, Customer customer, Throwable t) {
		
		CustomerDetails customerDetails = new CustomerDetails();
		customerDetails.setAccounts(accountsRepository.findByCustomerId(customer.getCustomerId()));
		customerDetails.setLoans(loansClient.getLoansDetails(correlationId, customer));
		
		return customerDetails;
	}
	
	
	@GetMapping("/sayHello")
	@RateLimiter(name = "sayHello", fallbackMethod = "sayHelloFallback")
	public String sayHello() {
		return "Hello, Welcome to PavanBank -> SayHello";
	}

	private String sayHelloFallback(Throwable t) {
		return "Hello, Welcome to PavanBank -> SayHelloFallBack";
	}
	
	private CustomerDetails myCustomerAccountsLoansDetailsFallback(String correlationId, Customer customer, Throwable t) {
		CustomerDetails customerDetails = new CustomerDetails();
		customerDetails.setAccounts(accountsRepository.findByCustomerId(customer.getCustomerId()));
		customerDetails.setLoans(loansClient.getLoansDetails(correlationId, customer));
		
		return customerDetails;
	}

}
