package dev.aprokhorenko.ticked_classifier;

import org.springframework.boot.SpringApplication;

public class TestTickedClassifierApplication {

	public static void main(String[] args) {
		SpringApplication.from(TickedClassifierApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
