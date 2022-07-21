/**
 * 
 */
package com.pavan.accounts.service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.pavan.accounts.model.Customer;
import com.pavan.accounts.model.Loans;

/**
 * @author PAVAN
 *
 */
@FeignClient("loans")
public interface LoansFeignClient {

	@RequestMapping(method = RequestMethod.POST, value = "myLoans", consumes = "application/json")
	List<Loans> getLoansDetails(@RequestHeader("pavanbank-correlation-id") String correlationid,@RequestBody Customer customer);
}
