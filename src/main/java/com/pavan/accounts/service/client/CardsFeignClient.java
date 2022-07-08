/**
 * 
 */
package com.pavan.accounts.service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.pavan.accounts.model.Cards;
import com.pavan.accounts.model.Customer;

/**
 * @author PAVAN
 *
 */
@FeignClient("cards")
public interface CardsFeignClient {

	@PostMapping(value = "myCards", consumes = "application/json")
	List<Cards> getCardDetails(@RequestHeader("pavanbank-correlation-id") String correlationId, @RequestBody Customer customer);
}
