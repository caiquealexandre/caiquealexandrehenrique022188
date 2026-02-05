package br.gov.mt.seletivo_seplag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SeletivoSeplagApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeletivoSeplagApplication.class, args);
	}

}
