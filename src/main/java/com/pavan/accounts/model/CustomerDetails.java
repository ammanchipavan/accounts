/**
 * 
 */
package com.pavan.accounts.model;

import java.util.List;

/**
 * @author PAVAN
 *
 */
public class CustomerDetails {
	private Accounts accounts;
	private List<Loans> loans;
	private List<Cards> cards;
	public Accounts getAccounts() {
		return accounts;
	}
	public void setAccounts(Accounts accounts) {
		this.accounts = accounts;
	}
	public List<Loans> getLoans() {
		return loans;
	}
	public void setLoans(List<Loans> loans) {
		this.loans = loans;
	}
	public List<Cards> getCards() {
		return cards;
	}
	public void setCards(List<Cards> cards) {
		this.cards = cards;
	}
}
