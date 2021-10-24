package org.cahucadi.reto.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ColumnTransformer;

import lombok.Data;

@Entity
@Table(name = "client")
@Data
public class Client {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;
	
	@Column(name = "code")
	private String code;
	
	@Transient
    private String decodedCode;
	
	@Column(name = "male")
	private short male;
	
	@Column(name = "type")
	private int type;
	
	@Column(name = "location")
	private String location;
	
	@Column(name = "company")
	private String company;
	
	@Column(name = "encrypt")
	private short encrypt;

	@Transient
    private double totalBalance;
	
	public Client() {
	}

	public Client(String code, short male, int type, String location, String company, short encrypt) {
		super();
		this.code = code;
		this.male = male;
		this.type = type;
		this.location = location;
		this.company = company;
		this.encrypt = encrypt;
	}
	
    @PostLoad
    public void decryptField() {
    	if(this.encrypt == 1) 
    		this.decodedCode = ClientAdapter.decryptService(this.code);
    	else
    		this.decodedCode = this.code;
    }
    
    
	
}
