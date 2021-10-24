package org.cahucadi.reto.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Data;

@Entity
@Table(name = "account")
@Data
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;
	
	@Column(name = "client_id")
	private long clientId;

	@Column(name = "balance", precision=10, scale=2)
	private double balance;
	
	
	public Account() {
	}

	public Account(long clientId, double balance) {
		super();
		this.clientId = clientId;
		this.balance = balance;
	}
	
}
