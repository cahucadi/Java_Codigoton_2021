package org.cahucadi.reto;

import org.cahucadi.reto.service.EvalartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EvalartRetoApplication  implements CommandLineRunner {

	@Autowired EvalartService service;
	
	public static void main(String[] args) {
		SpringApplication.run(EvalartRetoApplication.class, args);
	}
	
	@Override
    public void run(String... args) throws Exception {	
		service.execute();
	}

}
