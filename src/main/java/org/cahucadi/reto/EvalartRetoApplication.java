package org.cahucadi.reto;

import org.cahucadi.reto.domain.ClientAdapter;
import org.cahucadi.reto.service.RetoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EvalartRetoApplication  implements CommandLineRunner {

	@Autowired RetoService servicio;
	
	public static void main(String[] args) {
		SpringApplication.run(EvalartRetoApplication.class, args);
	}
	
	@Override
    public void run(String... args) throws Exception {	
		servicio.execute();
	}

}
