package br.project.portalapo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ApoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApoApplication.class, args);
	}

}
