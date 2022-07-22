/**
 * 
 */
package com.pavan.accounts.controller;

import java.util.List;
import java.util.Optional;

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
import com.pavan.accounts.model.Cards;
import com.pavan.accounts.model.Customer;
import com.pavan.accounts.model.CustomerDetails;
import com.pavan.accounts.model.Loans;
import com.pavan.accounts.model.Properties;
import com.pavan.accounts.repository.AccountsRepository;
import com.pavan.accounts.service.client.CardsFeignClient;
import com.pavan.accounts.service.client.LoansFeignClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;

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
	LoansFeignClient loansFeignClient;
	
	@Autowired
	CardsFeignClient cardsFeignClient;

	@PostMapping("/myAccount")
	@Timed(value = "getAccountDetails.time", description = "Time taken to return Account Details")
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
	@CircuitBreaker(name = "detailsForCustomerSupportApp", fallbackMethod = "myCustomerDetailsFallback")
	@Retry(name="retryForCustomerDetails", fallbackMethod = "myCustomerDetailsFallback")
	public CustomerDetails myCustomerDetails(@RequestHeader("pavanbank-correlation-id") String correlationId, @RequestBody Customer customer) {
		logger.info("myCustomerDetails() method started");
		
		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
		List<Loans> loans = loansFeignClient.getLoansDetails(correlationId,customer);
		List<Cards> cards = cardsFeignClient.getCardDetails(correlationId,customer);

		CustomerDetails customerDetails = new CustomerDetails();
		customerDetails.setAccounts(accounts);
		customerDetails.setLoans(loans);
		customerDetails.setCards(cards);
		logger.info("myCustomerDetails() method ended");
		
		return customerDetails;
	}
	
	private CustomerDetails myCustomerDetailsFallback(@RequestHeader("pavanbank-correlation-id") String correlationId, Customer customer, Throwable t) {
		
		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
		//List<Loans> loans = loansFeignClient.getLoansDetails(correlationId,customer);
		
		CustomerDetails customerDetails = new CustomerDetails();
		customerDetails.setAccounts(accounts);
		//customerDetails.setLoans(loans);
		
		return customerDetails;
	}
	
	
	@GetMapping("/sayHello")
	@RateLimiter(name = "sayHello", fallbackMethod = "sayHelloFallback")
	public String sayHello() {
		Optional<String> podName = Optional.ofNullable(System.getenv("HOSTNAME"));
		return "Hello, Welcome to PavanBank v1 -> SayHello, podName:" + podName.get();
	}

	private String sayHelloFallback(Throwable t) {
		return "Hello, Welcome to PavanBank  v1 -> SayHelloFallBack";
	}
	

}
